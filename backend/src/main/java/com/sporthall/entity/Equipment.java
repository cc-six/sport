package com.sporthall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("equipment")
public class Equipment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private Integer totalQty;
    private Integer availableQty;
    private BigDecimal pricePerHour;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
