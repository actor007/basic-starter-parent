package com.basic.dal.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.basic.common.context.TenantContextHolder;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

/**
 * 多租户 tenant_id 自动填充处理器
 * <p>
 * 在 INSERT 时自动从 TenantContextHolder 获取当前租户ID并填充到 tenant_id 字段。
 *
 * @author actor
 */
@Slf4j
@Component
public class TenantMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        String tenantId = TenantContextHolder.getTenantId();
        if (tenantId != null) {
            Object existTenantId = this.getFieldValByName("tenantId", metaObject);
            if (existTenantId == null) {
                this.setFieldValByName("tenantId", Long.valueOf(tenantId), metaObject);
                if (log.isDebugEnabled()) {
                    log.debug("Auto fill tenant_id: {}", tenantId);
                }
            }
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        // 更新时不修改 tenant_id
    }

}
