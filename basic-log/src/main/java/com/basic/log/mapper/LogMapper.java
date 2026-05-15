package com.basic.log.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.basic.log.model.LogDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作日志 Mapper
 *
 * @author actor
 * @date 2024/5/15
 */
@Mapper
public interface LogMapper extends BaseMapper<LogDO> {
}
