package com.basic.config;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * mybatis-plus自动填充器
 *
 * @author actor
 * @date 2022/4/26 11:49 AM
 */
@Slf4j
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        if (log.isInfoEnabled()) {
            log.info("start insert fill ...");
        }
        Object creatTime = this.getFieldValByName("createTime", metaObject);
        if (ObjectUtil.isEmpty(creatTime)) {
            this.setFieldValByName("createTime", new Date(), metaObject);
        }
        this.setFieldValByName("modifyTime", new Date(), metaObject);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        if (log.isInfoEnabled()) {
            log.info("start update fill .......");
        }
        this.setFieldValByName("modifyTime", new Date(), metaObject);
    }






}
