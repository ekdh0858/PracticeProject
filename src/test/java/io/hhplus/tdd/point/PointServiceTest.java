package io.hhplus.tdd.point;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {
    @Mock
    UserPointTable userPointTable;
    @Mock
    PointHistoryTable pointHistoryTable;

    @InjectMocks
    private PointService pointService;

    @Test
    void givenUser_whenGetPoint_thenReturnUserPoint() {
        // Given
        long id = 1L;
        when(userPointTable.selectById(id)).thenReturn(new UserPoint(id, 100L, 1111L));

        // When
        UserPoint result = pointService.getUserPointById(id);

        // Then
        assertEquals(id, result.id());
        assertEquals(100L, result.point());
//        포인트를 조회할때 어떤 것을 검증해야할지 모르겠습니다.
//        지금은 단순하게 조회한 사람의 포인트가 예상한 값과 맞는지 확인합니다.

//        단위 테스트라서 hashmap을 db라고 생각하여 select시 가상의 데이터를 나오게 하였습니다.
//        단위 테스트는 외부 의존성을 mock으로 대체하여서 db연결도 안하는게 맞다고 생각하는데, 이 생각이 맞는지 궁금합니다.
    }

    @Test
    void givenUser_whenGetAllHistoryById_thenReturnHistoryList() {
        // Given
        long userId = 3L;
        PointHistory h1 = new PointHistory(10L, userId, 50L, TransactionType.CHARGE, 1000L);
        PointHistory h2 = new PointHistory(11L, userId, 20L, TransactionType.USE, 2000L);
        when(pointHistoryTable.selectAllByUserId(userId)).thenReturn(List.of(h1, h2));

        // When
        List<PointHistory> histories = pointService.getAllHistoryById(userId);

        // Then
        assertEquals(2, histories.size());
        assertEquals(h1, histories.get(0));
        assertEquals(h2, histories.get(1));
//        포인트 내역을 조회하고 예상한 값과 맞는지 확인합니다.

//        개수가 달라진것만 확인해도 충분한지, 아니면 각 값들도 같은지
//        확인할 필요가 있는지 궁금합니다. (내용이 중요할 경우에는 확인할 필요가 있는것 같은데,일반적인 상황에서는 굳이 해야할까란 생각이 듭니다)
//        지금은 충전과 사용이 제대로 들어갔는지 확인하는게 중요하다고 생각해서 확인합니다.
    }

    @Test
    void givenUser_whenChargePoint_thenIncreasePointAndAddHistory() {
        // Given
        long userId = 5L;
        long amount = 40L;
        when(userPointTable.selectById(userId))
            .thenReturn(new UserPoint(userId, 100L, 1000L));
        when(userPointTable.insertOrUpdate(userId, 140L))
            .thenReturn(new UserPoint(userId, 140L, 2000L));

        // When
        UserPoint after = pointService.chargePoint(userId, amount);

        // Then
        assertEquals(140L, after.point());

        // history insert가 정확히 1번 호출됐는지 검증
        verify(pointHistoryTable, times(1))
            .insert(eq(userId), eq(amount), eq(TransactionType.CHARGE), anyLong());
//        충전을 하면 내역도 같이 업데이트가 되기때문에 한번에 내역과 포인트를 같이 확인합니다.
//        단위 테스트에서는 한 가지 책임만 검증하는게 좋다고 하고
//        포인트 로직의 문제인지 히스토리 로직의 문제인지 알아야 하면 나누는게 좋다고 생각은 하는데 너무 과한 투자는 아닌지 궁금합니다
//        단위 테스트에서는 저장을 확인할 수가 없으니깐 호출됬는지로 확인하는데 이런식으로 하는게 정상인가요?
    }

    @Test
    void givenSufficientPoint_whenUsePoint_thenDecreasePointAndAddHistory() {
        // Given
        long userId = 7L;
        long useAmount = 40L;
        when(userPointTable.selectById(userId)).thenReturn(new UserPoint(userId, 90L, 1000L));
        when(userPointTable.insertOrUpdate(userId, 50L))
            .thenReturn(new UserPoint(userId, 50L, 2000L));

        // When
        UserPoint after = pointService.usePoint(userId, useAmount);

        // Then
        assertEquals(50L, after.point());
        verify(pointHistoryTable, times(1))
            .insert(eq(userId), eq(useAmount), eq(TransactionType.USE), anyLong());
    }
    @Test
    void givenInsufficientPoint_whenUsePoint_thenThrow_andNoHistoryNorUpdate() {
        // Given
        long userId = 8L;
        long useAmount = 50L;
        when(userPointTable.selectById(userId)).thenReturn(new UserPoint(userId, 10L, 1000L));

        // When & Then
        IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> pointService.usePoint(userId, useAmount)
        );
        assertEquals("잔고가 부족합니다.", ex.getMessage());
        assertEquals(10L, pointService.getUserPointById(userId).point());

    }
//    사용 같은 경우 잔액이 모자랄 경우 금액 차감이 되면 안되기 때문에 금액 확인도 필요하다고 생각하여 금액 확인을 합니다.
//    역시 내역도 길이가 추가되었는지 확인합니다.

}