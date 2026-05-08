package com.sporthall.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sporthall.entity.Rental;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RentalMapper extends BaseMapper<Rental> {
}
