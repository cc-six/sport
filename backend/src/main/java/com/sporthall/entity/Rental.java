package com.sporthall.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("rental")
public class Rental {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long equipmentId;
    private Integer quantity;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    private LocalDateTime returnTime;
}
