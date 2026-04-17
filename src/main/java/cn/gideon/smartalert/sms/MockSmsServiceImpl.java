package cn.gideon.smartalert.sms;

import cn.gideon.smartalert.common.response.Result;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mock短信服务
 *
 * @author gideon
 */
@Slf4j
@Setter
public class MockSmsServiceImpl implements SmsService {

    private static Logger logger = LoggerFactory.getLogger(MockSmsServiceImpl.class);

//    @DistributeLock(scene = "SEND_SMS", keyExpression = "#phoneNumber")
    @Override
    public Result sendMsg(String phoneNumber, String code) {
        return Result.success(true);
    }
}
