package cn.gideon.smartalert.business.alert.mapper;

import cn.gideon.smartalert.business.alert.entity.AlertTask;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 推送任务Mapper
 */
@Mapper
public interface AlertTaskMapper extends BaseMapper<AlertTask> {
}
