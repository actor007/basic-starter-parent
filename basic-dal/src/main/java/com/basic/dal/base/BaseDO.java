package com.basic.dal.base;

import cn.hutool.core.date.DatePattern;
import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;

/**
 * 〈do 父类 数据库实体类〉
 *
 * @author actor
 * @create 2022/8/31
 */
@Getter
@Setter
@Accessors(chain = true)
public class BaseDO implements Serializable {

	@TableId(type = IdType.ASSIGN_ID)
	@JsonSerialize(using = ToStringSerializer.class)
	private Long id;

	/**
	 * 创建时间
	 */
	@TableField(fill = FieldFill.INSERT, value = "create_time")
	@JsonFormat(pattern = DatePattern.NORM_DATETIME_PATTERN, timezone = "GMT+8")
	private Date createTime;

	/**
	 * 更新时间
	 */
	@TableField(fill = FieldFill.INSERT_UPDATE, value = "modify_time")
	@JsonFormat(pattern = DatePattern.NORM_DATETIME_PATTERN, timezone = "GMT+8")
	private Date modifyTime;

	/**
	 * 是否删除(1:已删除0:未删除)
	 */
	@TableLogic
	@TableField("deleted")
	private Integer deleted;

	/**
	 * 时间戳
	 */
	@Version
	private Timestamp version;


}