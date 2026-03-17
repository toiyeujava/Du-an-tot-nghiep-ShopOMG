package poly.edu.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import poly.edu.dto.FlashSaleStatusDTO;
import poly.edu.dto.FlashVoucherDTO;
import poly.edu.entity.FlashSaleClaim;
import poly.edu.entity.Voucher;
import poly.edu.repository.FlashSaleClaimRepository;
import poly.edu.repository.OrderRepository;
import poly.edu.repository.VoucherRepository;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * FlashSaleService — Core logic for the Flash Sale Voucher system.
 *
 * Responsibilities:
 * 1. Generate daily flash sale voucher codes with pattern: FS-YYYYMMDD-XXXX
 * 2. Determine current flash sale status (UPCOMING / ACTIVE / ENDED)
 * 3. Calculate remaining time for countdown timer
 * 4. Provide voucher information for frontend display
 * 5. Handle per-user claim logic
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FlashSaleService {

    private final VoucherRepository voucherRepository;
    private final FlashSaleClaimRepository flashSaleClaimRepository;
    private final OrderRepository orderRepository;

    // ─── CONFIGURABLE CONSTANTS ─────────────────────────────────────────────
    private static final int FLASH_SALE_START_HOUR = 8;
    private static final int FLASH_SALE_END_HOUR = 20;
    private static final int FLASH_SALE_DISCOUNT_PERCENT = 15; // 15% giảm giá
    private static final BigDecimal MIN_ORDER_AMOUNT = BigDecimal.valueOf(200000); // 200k
    private static final BigDecimal MAX_DISCOUNT_AMOUNT = BigDecimal.valueOf(100000); // tối đa giảm 100k
    private static final int VOUCHER_QUANTITY = 50; // 50 lượt dùng mỗi ngày

    private static final String CODE_PREFIX = "FS";
    private static final String ALPHA_NUMERIC = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // exclude confusing chars: I,O,0,1
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * Generate a unique voucher code for a given date.
     * Pattern: FS-YYYYMMDD-XXXX (e.g., FS-20260315-K7W2)
     */
    public String generateVoucherCode(LocalDate date) {
        String dateStr = date.format(DATE_FMT);
        String code;
        int attempts = 0;
        do {
            StringBuilder suffix = new StringBuilder();
            for (int i = 0; i < 4; i++) {
                suffix.append(ALPHA_NUMERIC.charAt(RANDOM.nextInt(ALPHA_NUMERIC.length())));
            }
            code = CODE_PREFIX + "-" + dateStr + "-" + suffix;
            attempts++;
            if (attempts > 100) {
                throw new RuntimeException("Unable to generate unique voucher code after 100 attempts");
            }
        } while (voucherRepository.existsByCode(code));
        return code;
    }

    /**
     * Generate the daily flash sale voucher if one doesn't exist yet for today.
     * Called by FlashSaleScheduler at midnight and on application startup.
     */
    public void generateDailyFlashSaleVoucher() {
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();

        // Check if voucher for today already exists
        List<Voucher> existing = voucherRepository.findTodayFlashSaleVouchers(todayStart);
        if (!existing.isEmpty()) {
            log.info("Flash Sale voucher for {} already exists: {}", today, existing.get(0).getCode());
            return;
        }

        String code = generateVoucherCode(today);

        Voucher voucher = Voucher.builder()
                .code(code)
                .discountPercent(FLASH_SALE_DISCOUNT_PERCENT)
                .minOrderAmount(MIN_ORDER_AMOUNT)
                .maxDiscountAmount(MAX_DISCOUNT_AMOUNT)
                .startDate(today.atTime(FLASH_SALE_START_HOUR, 0))
                .endDate(today.atTime(FLASH_SALE_END_HOUR, 0))
                .quantity(VOUCHER_QUANTITY)
                .isActive(true)
                .isFlashSale(true)
                .flashSaleStartHour(FLASH_SALE_START_HOUR)
                .flashSaleEndHour(FLASH_SALE_END_HOUR)
                .build();

        voucherRepository.save(voucher);
        log.info("Generated Flash Sale voucher for {}: {} ({}% off, max {}đ, {} uses)",
                today, code, FLASH_SALE_DISCOUNT_PERCENT, MAX_DISCOUNT_AMOUNT, VOUCHER_QUANTITY);
    }

    /**
     * Get the current Flash Sale status for the frontend countdown timer.
     * Optionally includes per-user claim state if accountId is provided.
     */
    public FlashSaleStatusDTO getFlashSaleStatus(Integer accountId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        LocalTime currentTime = now.toLocalTime();

        LocalTime startTime = LocalTime.of(FLASH_SALE_START_HOUR, 0);
        LocalTime endTime = LocalTime.of(FLASH_SALE_END_HOUR, 0);

        String status;
        long remainingSeconds;

        if (currentTime.isBefore(startTime)) {
            status = "UPCOMING";
            remainingSeconds = ChronoUnit.SECONDS.between(now, today.atTime(startTime));
        } else if (currentTime.isBefore(endTime)) {
            status = "ACTIVE";
            remainingSeconds = ChronoUnit.SECONDS.between(now, today.atTime(endTime));
        } else {
            status = "ENDED";
            LocalDateTime tomorrowStart = today.plusDays(1).atTime(startTime);
            remainingSeconds = ChronoUnit.SECONDS.between(now, tomorrowStart);
        }

        // Fetch today's vouchers
        List<FlashVoucherDTO> vouchers = List.of();
        if ("ACTIVE".equals(status)) {
            List<Voucher> todayVouchers = voucherRepository.findTodayFlashSaleVouchers(today.atStartOfDay());

            // Build set of claimed voucher IDs for this user
            Set<Integer> claimedVoucherIds = Set.of();
            if (accountId != null) {
                claimedVoucherIds = flashSaleClaimRepository.findByAccountId(accountId).stream()
                        .map(FlashSaleClaim::getVoucherId)
                        .collect(Collectors.toSet());
            }

            final Set<Integer> finalClaimedIds = claimedVoucherIds;
            vouchers = todayVouchers.stream()
                    .map(v -> FlashVoucherDTO.builder()
                            .voucherId(v.getId())
                            .code(v.getCode())
                            .discountPercent(v.getDiscountPercent())
                            .discountAmount(v.getDiscountAmount())
                            .minOrderAmount(v.getMinOrderAmount())
                            .maxDiscountAmount(v.getMaxDiscountAmount())
                            .remainingQuantity(v.getQuantity())
                            .claimed(finalClaimedIds.contains(v.getId()))
                            .build())
                    .collect(Collectors.toList());
        }

        return FlashSaleStatusDTO.builder()
                .status(status)
                .remainingSeconds(remainingSeconds)
                .startHour(FLASH_SALE_START_HOUR)
                .endHour(FLASH_SALE_END_HOUR)
                .vouchers(vouchers)
                .build();
    }

    /**
     * Claim a flash sale voucher for a specific user.
     * Returns the voucher code on success, throws RuntimeException on failure.
     */
    public String claimVoucher(Integer accountId, Integer voucherId) {
        // 1. Find voucher
        Voucher voucher = voucherRepository.findById(voucherId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy voucher."));

        // 2. Verify it's flash sale + active + has quantity
        if (!Boolean.TRUE.equals(voucher.getIsFlashSale()) || !Boolean.TRUE.equals(voucher.getIsActive())) {
            throw new RuntimeException("Voucher Flash Sale này không khả dụng.");
        }
        if (voucher.getQuantity() <= 0) {
            throw new RuntimeException("Voucher Flash Sale đã hết lượt nhận. Hẹn bạn ngày mai!");
        }

        // 3. Check if user already claimed
        if (flashSaleClaimRepository.existsByAccountIdAndVoucherId(accountId, voucherId)) {
            throw new RuntimeException("Bạn đã nhận mã Flash Sale này rồi. Mỗi người chỉ được nhận 1 lần!");
        }

        // 4. Check if user already used a flash sale voucher today (via order)
        if (orderRepository.hasUserUsedVoucher(accountId, voucherId)) {
            throw new RuntimeException("Bạn đã sử dụng mã Flash Sale này rồi.");
        }

        // 5. Decrement quantity and save claim
        voucher.setQuantity(voucher.getQuantity() - 1);
        voucherRepository.save(voucher);

        FlashSaleClaim claim = FlashSaleClaim.builder()
                .accountId(accountId)
                .voucherId(voucherId)
                .claimedAt(LocalDateTime.now())
                .build();
        flashSaleClaimRepository.save(claim);

        log.info("User {} claimed Flash Sale voucher {} (code: {})", accountId, voucherId, voucher.getCode());
        return voucher.getCode();
    }

    /**
     * Get today's claimed (but not yet used) flash sale vouchers for a user.
     * Used in checkout to show available flash sale vouchers.
     */
    public List<Voucher> getClaimedUnusedFlashVouchers(Integer accountId) {
        List<FlashSaleClaim> claims = flashSaleClaimRepository.findByAccountId(accountId);
        LocalDate today = LocalDate.now();

        return claims.stream()
                .map(claim -> voucherRepository.findById(claim.getVoucherId()).orElse(null))
                .filter(v -> v != null
                        && Boolean.TRUE.equals(v.getIsFlashSale())
                        && Boolean.TRUE.equals(v.getIsActive())
                        && v.getStartDate() != null
                        && v.getStartDate().toLocalDate().isEqual(today)
                        && !orderRepository.hasUserUsedVoucher(accountId, v.getId()))
                .collect(Collectors.toList());
    }
}
