package com.gijun.backend.service;

import com.gijun.backend.domain.dto.order.TransactionType;
import com.gijun.backend.domain.entity.*;
import com.gijun.backend.repository.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
@AllArgsConstructor
public class InstitutionTradingService {

    private final InstitutionRepository institutionRepository;
    private final InstitutionCharacteristicsRepository characteristicsRepository;
    private final StockRepository stockRepository;
    private final InstitutionHoldingRepository holdingRepository;
    private final InstitutionTransactionRepository transactionRepository;
    private final Random random = new Random();

    public void executeBatchTrading() {
        List<Institution> institutions = institutionRepository.findAll();
        List<Stock> stocks = stockRepository.findAllByIsTrading(true);

        for (Institution institution : institutions) {
            try {
                processInstitutionTrading(institution, stocks);
            } catch (Exception e) {
                log.error("Error processing trading for institution {}: {}", institution.getCode(), e.getMessage());
            }
        }
    }

    private void processInstitutionTrading(Institution institution, List<Stock> stocks) {
        InstitutionCharacteristics characteristics = characteristicsRepository.findByInstitution(institution)
                .orElseThrow(() -> new IllegalStateException("No characteristics found for institution " + institution.getCode()));

        // 거래 빈도에 따른 거래 확률 계산
        if (random.nextDouble() > characteristics.getTradingFrequency()) {
            return;
        }

        // 현재 보유 종목 분석
        List<InstitutionHolding> holdings = holdingRepository.findByInstitution(institution);
        Map<Long, InstitutionHolding> holdingMap = holdings.stream()
                .collect(Collectors.toMap(h -> h.getStock().getId(), h -> h));

        // 매수/매도 결정 및 실행
        for (Stock stock : stocks) {
            try {
                processStockTrading(institution, stock, characteristics, holdingMap.get(stock.getId()));
            } catch (Exception e) {
                log.error("Error processing trading for stock {}: {}", stock.getStockCode(), e.getMessage());
            }
        }
    }

    private void processStockTrading(Institution institution, Stock stock,
                                     InstitutionCharacteristics characteristics,
                                     InstitutionHolding currentHolding) {
        // 투자 결정을 위한 기본 확률 계산 (리스크 성향 반영)
        double baseProb = characteristics.getRiskTolerance();

        // 거래 스타일에 따른 추가 확률 조정
        switch (characteristics.getTradingStyle()) {
            case CONSERVATIVE:
                baseProb *= 0.7;
                break;
            case AGGRESSIVE:
                baseProb *= 1.3;
                break;
            case VALUE_FOCUSED:
                // 시장가격이 기준가격보다 낮을 때 매수 확률 증가
                if (stock.getCurrentPrice().compareTo(stock.getBasePrice()) < 0) {
                    baseProb *= 1.2;
                }
                break;
            case MOMENTUM_BASED:
                // 상승 추세일 때 매수 확률 증가
                if (stock.getCurrentPrice().compareTo(stock.getBasePrice()) > 0) {
                    baseProb *= 1.2;
                }
                break;
        }

        // 최종 거래 결정
        if (currentHolding != null) {  // 보유 중인 종목인 경우
            if (random.nextDouble() < baseProb) {
                if (random.nextBoolean()) {  // 50% 확률로 매수 또는 매도
                    executePurchase(institution, stock, characteristics);
                } else {
                    executeSale(institution, stock, currentHolding, characteristics);
                }
            }
        } else {  // 보유하지 않은 종목인 경우
            // 더 낮은 확률로 신규 매수 시도
            if (random.nextDouble() < baseProb * 0.3) {  // 30% 낮은 확률
                executePurchase(institution, stock, characteristics);
            }
        }
    }

    private void executePurchase(Institution institution, Stock stock,
                                 InstitutionCharacteristics characteristics) {
        // 포지션 한도 확인
        BigDecimal currentPosition = calculateCurrentPosition(institution, stock);
        if (currentPosition.compareTo(BigDecimal.valueOf(characteristics.getPositionLimit())) >= 0) {
            return;
        }

        // 거래 수량 결정 (현금 잔고와 리스크 성향 고려)
        int quantity = calculatePurchaseQuantity(institution, stock, characteristics);
        if (quantity <= 0) return;

        // 거래 기록
        InstitutionTransaction transaction = InstitutionTransaction.builder()
                .institution(institution)
                .stock(stock)
                .type(TransactionType.BUY)
                .quantity(quantity)
                .price(stock.getCurrentPrice())
                .build();

        transactionRepository.save(transaction);
        updateHolding(institution, stock, quantity, true);
        updateInstitutionCash(institution, stock.getCurrentPrice().multiply(BigDecimal.valueOf(quantity)), false);
    }

    private void executeSale(Institution institution, Stock stock,
                             InstitutionHolding holding, InstitutionCharacteristics characteristics) {
        // 매도 수량 결정 (보유 수량과 리스크 성향 고려)
        int quantity = calculateSaleQuantity(holding, characteristics);
        if (quantity <= 0) return;

        // 거래 기록
        InstitutionTransaction transaction = InstitutionTransaction.builder()
                .institution(institution)
                .stock(stock)
                .type(TransactionType.SELL)
                .quantity(quantity)
                .price(stock.getCurrentPrice())
                .build();

        transactionRepository.save(transaction);
        updateHolding(institution, stock, quantity, false);
        updateInstitutionCash(institution, stock.getCurrentPrice().multiply(BigDecimal.valueOf(quantity)), true);
    }

    private BigDecimal calculateCurrentPosition(Institution institution, Stock stock) {
        // 해당 종목에 대한 현재 포지션 비율 계산
        Optional<InstitutionHolding> holding = holdingRepository.findByInstitutionAndStock(institution, stock);
        if (holding.isEmpty()) {
            return BigDecimal.ZERO;
        }

        // 해당 종목의 현재 가치
        BigDecimal stockValue = stock.getCurrentPrice()
                .multiply(BigDecimal.valueOf(holding.get().getQuantity()));

        // 전체 자산 대비 비율 계산 (%)
        return stockValue
                .divide(institution.getTotalAsset(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    private int calculatePurchaseQuantity(Institution institution, Stock stock,
                                          InstitutionCharacteristics characteristics) {
        // 현재 현금 잔고 확인
        BigDecimal availableCash = institution.getCashBalance()
                .multiply(BigDecimal.valueOf(1 - characteristics.getCashReserveRatio() / 100));
        if (availableCash.compareTo(BigDecimal.ZERO) <= 0) {
            return 0;
        }

        // 리스크 성향에 따른 최대 거래 금액 계산
        BigDecimal maxTradeAmount = availableCash
                .multiply(BigDecimal.valueOf(characteristics.getRiskTolerance()));

        // 현재 주가 기준으로 매수 가능한 최대 수량 계산
        int maxQuantity = maxTradeAmount
                .divide(stock.getCurrentPrice(), 0, RoundingMode.DOWN)
                .intValue();

        // 거래 단위 조정 (10주 단위)
        maxQuantity = (maxQuantity / 10) * 10;

        // 랜덤성 부여 (50~100% 사이)
        double randomFactor = 0.5 + random.nextDouble() * 0.5;
        int quantity = (int) (maxQuantity * randomFactor);

        return Math.max(10, quantity); // 최소 10주
    }

    private int calculateSaleQuantity(InstitutionHolding holding,
                                      InstitutionCharacteristics characteristics) {
        int currentQuantity = holding.getQuantity();
        if (currentQuantity <= 0) {
            return 0;
        }

        // 리스크 성향에 따른 매도 비율 결정
        double sellRatio;
        // 리스크 성향이 높을수록 한 번에 많은 수량을 매도
        if (characteristics.getRiskTolerance() > 0.7) {
            sellRatio = 0.3 + random.nextDouble() * 0.4; // 30~70%
        } else {
            sellRatio = 0.1 + random.nextDouble() * 0.3; // 10~40%
        }

        // 매도 수량 계산 (10주 단위 조정)
        int quantity = (int) (currentQuantity * sellRatio);
        quantity = (quantity / 10) * 10;

        return Math.max(10, Math.min(quantity, currentQuantity));
    }

    private void updateHolding(Institution institution, Stock stock, int quantity, boolean isPurchase) {
        InstitutionHolding holding = holdingRepository
                .findByInstitutionAndStock(institution, stock)
                .orElse(null);

        if (isPurchase) {
            if (holding == null) {
                // 새로운 보유 생성
                holding = InstitutionHolding.builder()
                        .institution(institution)
                        .stock(stock)
                        .quantity(quantity)
                        .averagePrice(stock.getCurrentPrice())
                        .build();
            } else {
                // 기존 보유 수정 (새 객체 생성하지 않음)
                BigDecimal totalValue = holding.getAveragePrice()
                        .multiply(BigDecimal.valueOf(holding.getQuantity()))
                        .add(stock.getCurrentPrice().multiply(BigDecimal.valueOf(quantity)));
                int totalQuantity = holding.getQuantity() + quantity;

                BigDecimal newAveragePrice = totalValue
                        .divide(BigDecimal.valueOf(totalQuantity), 2, RoundingMode.HALF_UP);

                // 값만 업데이트
                holding.updateQuantityAndPrice(totalQuantity, newAveragePrice);
            }
        } else {
            if (holding != null) {
                int remainingQuantity = holding.getQuantity() - quantity;
                if (remainingQuantity <= 0) {
                    holdingRepository.delete(holding);
                    return;
                }

                // 값만 업데이트
                holding.updateQuantityAndPrice(remainingQuantity, holding.getAveragePrice());
            }
        }

        if (holding != null) {
            holdingRepository.save(holding);
        }
    }

    private void updateInstitutionCash(Institution institution, BigDecimal amount, boolean isIncrease) {
        BigDecimal currentCash = institution.getCashBalance();
        BigDecimal newCash;

        if (isIncrease) {
            newCash = currentCash.add(amount);
        } else {
            newCash = currentCash.subtract(amount);
        }

        // 직접 엔티티 업데이트는 지양하고 별도의 업데이트 메서드를 통해 처리하는 것이 좋습니다
        institution.updateCashBalance(newCash);
        institutionRepository.save(institution);
    }
}