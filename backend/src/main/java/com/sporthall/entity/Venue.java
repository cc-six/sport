package com.sporthall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@TableName("venue")
public class Venue {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String type;
    private Integer status;
    private LocalTime openTime;
    private LocalTime closeTime;
    private BigDecimal pricePerHour;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
