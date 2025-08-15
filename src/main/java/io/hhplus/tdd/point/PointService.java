package io.hhplus.tdd.point;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class PointService {
    private final UserPointTable userPointTable;
    private final PointHistoryTable pointHistoryTable;

    public UserPoint getUserPointById(long id){
        return userPointTable.selectById(id);
    }

    public List<PointHistory> getAllHistoryById(long userId){
        return pointHistoryTable.selectAllByUserId(userId);
    }

    public UserPoint chargePoint(long userId, long amount){
        UserPoint userPoint = userPointTable.selectById(userId);
        long currentPoint = userPoint.point();
        pointHistoryTable.insert(userId,amount,TransactionType.CHARGE, System.currentTimeMillis());
        return userPointTable.insertOrUpdate(userId,currentPoint+amount);
    }

    public UserPoint usePoint(long userId, long amount){
        UserPoint userPoint = userPointTable.selectById(userId);
        long currentPoint = userPoint.point();
        if (amount > currentPoint) {
            throw new IllegalArgumentException("잔고가 부족합니다.");
        }
        pointHistoryTable.insert(userId,amount,TransactionType.USE, System.currentTimeMillis());
        return userPointTable.insertOrUpdate(userId,currentPoint-amount);
    }

}
