package com.vbs.demo.controller;

import com.vbs.demo.dto.ScheduledPaymentDto;
import com.vbs.demo.models.*;
import com.vbs.demo.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class SmartTransactionController {

    @Autowired ScheduledPaymentRepo scheduledPaymentRepo;
    @Autowired BeneficiaryRepo beneficiaryRepo;
    @Autowired TransactionRepo transactionRepo;
    @Autowired UserRepo userRepo;

    // ── BENEFICIARIES ──────────────────────────────────

    @GetMapping("/beneficiaries/{userId}")
    public List<Beneficiary> getBeneficiaries(@PathVariable int userId) {
        return beneficiaryRepo.findAllByUserId(userId);
    }

    @PostMapping("/beneficiaries/add")
    public String addBeneficiary(@RequestBody Beneficiary b) {
        if (beneficiaryRepo.existsByUserIdAndUsername(b.getUserId(), b.getUsername()))
            return "Already in favourites";
        User u = userRepo.findByUsername(b.getUsername());
        if (u == null) return "User not found";
        beneficiaryRepo.save(b);
        return "Added to favourites";
    }

    @DeleteMapping("/beneficiaries/remove/{userId}/{username}")
    public String removeBeneficiary(@PathVariable int userId, @PathVariable String username) {
        beneficiaryRepo.deleteByUserIdAndUsername(userId, username);
        return "Removed from favourites";
    }

    // ── SCHEDULED PAYMENTS ─────────────────────────────

    @GetMapping("/scheduled/{userId}")
    public List<ScheduledPayment> getScheduled(@PathVariable int userId) {
        return scheduledPaymentRepo.findAllByUserId(userId);
    }

    @PostMapping("/scheduled/add")
    public String addScheduled(@RequestBody ScheduledPaymentDto dto) {
        User rec = userRepo.findByUsername(dto.getRecipientUsername());
        if (rec == null) return "Recipient not found";
        ScheduledPayment sp = new ScheduledPayment();
        sp.setUserId(dto.getUserId());
        sp.setRecipientUsername(dto.getRecipientUsername());
        sp.setAmount(dto.getAmount());
        sp.setLabel(dto.getLabel());
        sp.setFrequency(dto.getFrequency());
        sp.setDayOfMonth(dto.getDayOfMonth());
        sp.setActive(true);
        sp.setNextRun(calcNextRun(dto.getDayOfMonth()));
        scheduledPaymentRepo.save(sp);
        return "Scheduled payment created";
    }

    @DeleteMapping("/scheduled/delete/{id}")
    public String deleteScheduled(@PathVariable int id) {
        scheduledPaymentRepo.deleteById(id);
        return "Deleted";
    }

    @PutMapping("/scheduled/toggle/{id}")
    public String toggleScheduled(@PathVariable int id) {
        ScheduledPayment sp = scheduledPaymentRepo.findById(id).orElseThrow();
        sp.setActive(!sp.isActive());
        scheduledPaymentRepo.save(sp);
        return sp.isActive() ? "Activated" : "Paused";
    }

    // ── AUTO-RUN every day at 9 AM ─────────────────────
    @Scheduled(cron = "0 0 9 * * *")
    public void runScheduledPayments() {
        List<ScheduledPayment> payments = scheduledPaymentRepo.findAllByActiveTrue();
        LocalDateTime now = LocalDateTime.now();
        for (ScheduledPayment sp : payments) {
            if (sp.getNextRun() != null && now.isBefore(sp.getNextRun())) continue;
            User sender = userRepo.findById(sp.getUserId()).orElse(null);
            User rec = userRepo.findByUsername(sp.getRecipientUsername());
            if (sender == null || rec == null) continue;
            double sbal = sender.getBalance() - sp.getAmount();
            if (sbal < 0) continue;
            double rbal = rec.getBalance() + sp.getAmount();
            sender.setBalance(sbal); rec.setBalance(rbal);
            userRepo.save(sender); userRepo.save(rec);
            Transaction t1 = new Transaction();
            t1.setAmount(sp.getAmount()); t1.setCurrBalance(sbal);
            t1.setDescription("Auto-pay: " + sp.getLabel() + " to " + sp.getRecipientUsername());
            t1.setNote("Scheduled " + sp.getFrequency() + " payment");
            t1.setUserId(sp.getUserId());
            Transaction t2 = new Transaction();
            t2.setAmount(sp.getAmount()); t2.setCurrBalance(rbal);
            t2.setDescription("Auto-pay received: " + sp.getLabel() + " from " + sender.getUsername());
            t2.setUserId(rec.getId());
            transactionRepo.save(t1); transactionRepo.save(t2);
            sp.setLastRun(now);
            sp.setNextRun(calcNextRun(sp.getDayOfMonth()));
            scheduledPaymentRepo.save(sp);
        }
    }

    private LocalDateTime calcNextRun(int day) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime next = now.withDayOfMonth(Math.min(day, now.toLocalDate().lengthOfMonth()))
                .withHour(9).withMinute(0).withSecond(0);
        if (!next.isAfter(now)) next = next.plusMonths(1);
        return next;
    }
}