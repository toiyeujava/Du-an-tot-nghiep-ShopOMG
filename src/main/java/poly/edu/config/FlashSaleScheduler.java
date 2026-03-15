package poly.edu.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import poly.edu.service.FlashSaleService;

/**
 * FlashSaleScheduler — Automatically generates Flash Sale vouchers daily.
 *
 * - Runs at midnight (00:00) every day to create that day's voucher.
 * - Also runs on application startup (@PostConstruct) to handle
 *   cases where the app starts after midnight.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FlashSaleScheduler {

    private final FlashSaleService flashSaleService;

    /**
     * Generate today's flash sale voucher on application startup.
     * Ensures a voucher exists even if the app was restarted during the day.
     */
    @PostConstruct
    public void onStartup() {
        log.info("FlashSaleScheduler: Checking/generating today's Flash Sale voucher on startup...");
        try {
            flashSaleService.generateDailyFlashSaleVoucher();
        } catch (Exception e) {
            log.error("Failed to generate Flash Sale voucher on startup", e);
        }
    }

    /**
     * CRON job: runs at 00:00:00 every day to create next day's voucher.
     */
    @Scheduled(cron = "0 0 0 * * *")
    public void generateDailyVoucher() {
        log.info("FlashSaleScheduler: Midnight cron - generating today's Flash Sale voucher...");
        try {
            flashSaleService.generateDailyFlashSaleVoucher();
        } catch (Exception e) {
            log.error("Failed to generate Flash Sale voucher via cron", e);
        }
    }
}
