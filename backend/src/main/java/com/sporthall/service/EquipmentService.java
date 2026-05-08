package com.sporthall.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sporthall.entity.Equipment;
import com.sporthall.entity.Rental;
import com.sporthall.interceptor.JwtInterceptor;
import com.sporthall.mapper.EquipmentMapper;
import com.sporthall.mapper.RentalMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EquipmentService {

    private final EquipmentMapper equipmentMapper;
    private final RentalMapper rentalMapper;

    public Page<Equipment> list(int page, int pageSize) {
        return equipmentMapper.selectPage(new Page<>(page, pageSize), null);
    }

    public Page<Rental> listRentals(Long userId, Integer status, int page, int pageSize) {
        QueryWrapper<Rental> wrapper = new QueryWrapper<>();
        if (userId != null) {
            wrapper.eq("user_id", userId);
        }
        if (status != null) {
            wrapper.eq("status", status);
        }
        wrapper.orderByDesc("create_time");
        return rentalMapper.selectPage(new Page<>(page, pageSize), wrapper);
    }

    public void add(Equipment equipment) {
        validateEquipment(equipment);
        if (equipment.getAvailableQty() == null) {
            equipment.setAvailableQty(equipment.getTotalQty());
        }
        if (equipment.getAvailableQty() < 0 || equipment.getAvailableQty() > equipment.getTotalQty()) {
            throw new RuntimeException("可借数量不合法");
        }
        equipmentMapper.insert(equipment);
    }

    @Transactional
    public void update(Equipment equipment) {
        Equipment old = equipmentMapper.selectById(equipment.getId());
        if (old == null) throw new RuntimeException("器材不存在");
        validateEquipment(equipment);
        int rentedQty = old.getTotalQty() - old.getAvailableQty();
        if (equipment.getTotalQty() < rentedQty) {
            throw new RuntimeException("总数量不能小于当前已借出数量");
        }
        equipment.setAvailableQty(equipment.getTotalQty() - rentedQty);
        equipmentMapper.updateById(equipment);
    }

    @Transactional
    public Rental rent(Long userId, Long equipmentId, int quantity) {
        Equipment equipment = equipmentMapper.selectById(equipmentId);
        if (equipment == null) throw new RuntimeException("器材不存在");
        if (quantity <= 0) throw new RuntimeException("租借数量必须大于0");
        if (equipment.getAvailableQty() < quantity) throw new RuntimeException("可借数量不足");

        equipment.setAvailableQty(equipment.getAvailableQty() - quantity);
        equipmentMapper.updateById(equipment);

        Rental rental = new Rental();
        rental.setUserId(userId);
        rental.setEquipmentId(equipmentId);
        rental.setQuantity(quantity);
        rental.setStatus(0);
        rentalMapper.insert(rental);
        return rental;
    }

    @Transactional
    public void returnRental(Long rentalId) {
        Rental rental = rentalMapper.selectById(rentalId);
        if (rental == null) throw new RuntimeException("租借记录不存在");
        if (rental.getStatus() != 0) throw new RuntimeException("状态异常");

        Equipment equipment = equipmentMapper.selectById(rental.getEquipmentId());
        equipment.setAvailableQty(equipment.getAvailableQty() + rental.getQuantity());
        equipmentMapper.updateById(equipment);

        rental.setStatus(1);
        rental.setReturnTime(LocalDateTime.now());
        rentalMapper.updateById(rental);
    }

    private void validateEquipment(Equipment equipment) {
        if (equipment.getName() == null || equipment.getName().trim().isEmpty()) throw new RuntimeException("器材名称不能为空");
        if (equipment.getTotalQty() == null || equipment.getTotalQty() < 0) throw new RuntimeException("器材总数量不合法");
        if (equipment.getPricePerHour() == null || equipment.getPricePerHour().signum() < 0) throw new RuntimeException("租借单价不能小于0");
    }
}
