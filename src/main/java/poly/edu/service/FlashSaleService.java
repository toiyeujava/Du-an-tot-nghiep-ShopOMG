package poly.edu.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import poly.edu.dto.FlashSaleStatusDTO;
import poly.edu.dto.FlashVoucherDTO;
import poly.edu.entity.Voucher;
import poly.edu.repository.VoucherRepository;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

/**
 * FlashSaleService — Core logic for the Flash Sale Voucher system.
 *
 * Responsibilities:
 * 1. Generate daily flash sale voucher codes with pattern: FS-YYYYMMDD-XXXX
 * 2. Determine current flash sale status (UPCOMING / ACTIVE / ENDED)
 * 3. Calculate remaining time for countdown timer
 * 4. Provide voucher information for frontend display
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FlashSaleService {

    private final VoucherRepository voucherRepository;

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
     * Returns one of three states:
     * - UPCOMING: before startHour, countdown to start
     * - ACTIVE: between startHour and endHour, countdown to end + voucher list
     * - ENDED: after endHour, shows "see you tomorrow" message
     */
    public FlashSaleStatusDTO getFlashSaleStatus() {
        LocalDateTime now = LocalDateTime.now();
        LocalDate today = now.toLocalDate();
        LocalTime currentTime = now.toLocalTime();

        LocalTime startTime = LocalTime.of(FLASH_SALE_START_HOUR, 0);
        LocalTime endTime = LocalTime.of(FLASH_SALE_END_HOUR, 0);

        String status;
        long remainingSeconds;

        if (currentTime.isBefore(startTime)) {
            // UPCOMING: before 11:00
            status = "UPCOMING";
            remainingSeconds = ChronoUnit.SECONDS.between(now, today.atTime(startTime));
        } else if (currentTime.isBefore(endTime)) {
            // ACTIVE: between 11:00 and 13:00
            status = "ACTIVE";
            remainingSeconds = ChronoUnit.SECONDS.between(now, today.atTime(endTime));
        } else {
            // ENDED: after 13:00
            status = "ENDED";
            // Countdown to tomorrow's flash sale start
            LocalDateTime tomorrowStart = today.plusDays(1).atTime(startTime);
            remainingSeconds = ChronoUnit.SECONDS.between(now, tomorrowStart);
        }

        // Fetch today's vouchers
        List<FlashVoucherDTO> vouchers = List.of();
        if ("ACTIVE".equals(status)) {
            List<Voucher> todayVouchers = voucherRepository.findTodayFlashSaleVouchers(today.atStartOfDay());
            vouchers = todayVouchers.stream()
                    .map(v -> FlashVoucherDTO.builder()
                            .code(v.getCode())
                            .discountPercent(v.getDiscountPercent())
                            .discountAmount(v.getDiscountAmount())
                            .minOrderAmount(v.getMinOrderAmount())
                            .maxDiscountAmount(v.getMaxDiscountAmount())
                            .remainingQuantity(v.getQuantity())
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
}
