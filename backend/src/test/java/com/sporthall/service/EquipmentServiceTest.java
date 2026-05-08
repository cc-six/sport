package com.sporthall.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sporthall.entity.Equipment;
import com.sporthall.entity.Rental;
import com.sporthall.mapper.EquipmentMapper;
import com.sporthall.mapper.RentalMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EquipmentService 单元测试")
class EquipmentServiceTest {

    @Mock
    private EquipmentMapper equipmentMapper;

    @Mock
    private RentalMapper rentalMapper;

    @InjectMocks
    private EquipmentService equipmentService;

    private Equipment testEquipment;
    private Long testUserId;

    @BeforeEach
    void setUp() {
        testUserId = 1L;

        testEquipment = new Equipment();
        testEquipment.setId(1L);
        testEquipment.setName("羽毛球拍");
        testEquipment.setTotalQty(20);
        testEquipment.setAvailableQty(15);
        testEquipment.setPricePerHour(new BigDecimal("10.00"));
    }

    @Test
    @DisplayName("器材列表 - 分页查询")
    void list_pagination() {
        Page<Equipment> expectedPage = new Page<>(1, 10);
        expectedPage.setRecords(java.util.Arrays.asList(testEquipment));
        when(equipmentMapper.selectPage(any(Page.class), isNull()))
                .thenReturn(expectedPage);

        Page<Equipment> result = equipmentService.list(1, 10);

        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
    }

    @Test
    @DisplayName("添加器材")
    void add_success() {
        equipmentService.add(testEquipment);

        verify(equipmentMapper).insert(testEquipment);
    }

    @Test
    @DisplayName("更新器材")
    void update_success() {
        when(equipmentMapper.selectById(1L)).thenReturn(testEquipment);

        equipmentService.update(testEquipment);

        verify(equipmentMapper).updateById(testEquipment);
    }

    @Test
    @DisplayName("租借器材 - 成功")
    void rent_success() {
        when(equipmentMapper.selectById(1L)).thenReturn(testEquipment);
        when(rentalMapper.insert(any(Rental.class))).thenAnswer(invocation -> {
            Rental rental = invocation.getArgument(0);
            rental.setId(1L);
            return 1;
        });

        Rental result = equipmentService.rent(testUserId, 1L, 2);

        assertNotNull(result);
        assertEquals(testUserId, result.getUserId());
        assertEquals(1L, result.getEquipmentId());
        assertEquals(2, result.getQuantity());
        assertEquals(0, result.getStatus());

        // Verify stock was deducted
        assertEquals(13, testEquipment.getAvailableQty());
        verify(equipmentMapper).updateById(testEquipment);
        verify(rentalMapper).insert(any(Rental.class));
    }

    @Test
    @DisplayName("租借器材 - 器材不存在")
    void rent_equipmentNotFound() {
        when(equipmentMapper.selectById(1L)).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                equipmentService.rent(testUserId, 1L, 2));
        assertEquals("器材不存在", ex.getMessage());
    }

    @Test
    @DisplayName("租借器材 - 库存不足")
    void rent_insufficientStock() {
        testEquipment.setAvailableQty(1);
        when(equipmentMapper.selectById(1L)).thenReturn(testEquipment);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                equipmentService.rent(testUserId, 1L, 2));
        assertEquals("可借数量不足", ex.getMessage());

        // Verify stock was NOT changed
        assertEquals(1, testEquipment.getAvailableQty());
        verify(equipmentMapper, never()).updateById(any());
    }

    @Test
    @DisplayName("租借器材 - 租借数量为0或负数")
    void rent_zeroQuantity() {
        when(equipmentMapper.selectById(1L)).thenReturn(testEquipment);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                equipmentService.rent(testUserId, 1L, 0));
        assertEquals("租借数量必须大于0", ex.getMessage());
    }

    @Test
    @DisplayName("归还器材 - 成功")
    void returnRental_success() {
        Rental rental = new Rental();
        rental.setId(1L);
        rental.setUserId(testUserId);
        rental.setEquipmentId(1L);
        rental.setQuantity(3);
        rental.setStatus(0);

        testEquipment.setAvailableQty(12);

        when(rentalMapper.selectById(1L)).thenReturn(rental);
        when(equipmentMapper.selectById(1L)).thenReturn(testEquipment);

        equipmentService.returnRental(1L);

        assertEquals(15, testEquipment.getAvailableQty()); // 12 + 3
        assertEquals(1, rental.getStatus());
        assertNotNull(rental.getReturnTime());

        verify(equipmentMapper).updateById(testEquipment);
        verify(rentalMapper).updateById(rental);
    }

    @Test
    @DisplayName("归还器材 - 租借记录不存在")
    void returnRental_notFound() {
        when(rentalMapper.selectById(1L)).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                equipmentService.returnRental(1L));
        assertEquals("租借记录不存在", ex.getMessage());
    }

    @Test
    @DisplayName("归还器材 - 已归还状态")
    void returnRental_alreadyReturned() {
        Rental rental = new Rental();
        rental.setId(1L);
        rental.setEquipmentId(1L);
        rental.setQuantity(3);
        rental.setStatus(1); // already returned

        when(rentalMapper.selectById(1L)).thenReturn(rental);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                equipmentService.returnRental(1L));
        assertEquals("状态异常", ex.getMessage());

        // Verify no stock changes
        verify(equipmentMapper, never()).updateById(any());
    }
}
