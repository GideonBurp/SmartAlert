package cn.gideon.smartalert.sms;


import cn.gideon.smartalert.common.response.Result;

/**
 * 短信服务
 *
 * @author gideon
 */
public interface SmsService {
    /**
     * 发送短信
     *
     * @param phoneNumber
     * @param code
     * @return
     */
    public Result sendMsg(String phoneNumber, String code);
}
