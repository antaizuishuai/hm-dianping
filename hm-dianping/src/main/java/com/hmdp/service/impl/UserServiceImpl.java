package com.hmdp.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RegexUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.rmi.CORBA.Util;
import javax.servlet.http.HttpSession;

import static com.hmdp.utils.SystemConstants.USER_NICK_NAME_PREFIX;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author 虎哥
 * @since 2021-12-22
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Override
    public Result sendCode(String phone, HttpSession session) {
        //1、校验手机号
        if (RegexUtils.isPhoneInvalid(phone)) {
            return Result.fail("手机号格式错误");
            //2、如果不符合，返回结果
        }
        //3、符合，返回验证码
        String code = RandomUtil.randomNumbers(6);
        //4、保存验证码到session
        session.setAttribute("code",code);
        //5、发送验证码
      log.debug("发送验证码成功，验证码：{}",code);
        //返回ok
        return Result.ok();
    }

    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        String phone = loginForm.getPhone();
        //1、校验手机号
        if (RegexUtils.isPhoneInvalid(loginForm.getPhone())) {
            return Result.fail("手机号格式错误");
        }
        //2、校验验证码
        Object cacheCode = session.getAttribute("code");
        if (cacheCode == null || !cacheCode.equals(loginForm.getCode())){
            return Result.fail("验证码错误");
        }
        //3、根据手机号查用户 select * from tb_user where phone =
        User user = query().eq("phone", phone).one();

        //4、判断用户是否存在
        if (user == null){
            //用户不存在,创建用户并保存
           user = createUserWithPhone(phone);
        }
        //5、保存用户信息到session
        session.setAttribute("user",user);
        return null;
    }

    private User createUserWithPhone(String phone) {
        User user = new User();
        user.setPhone(phone);
        user.setNickName(USER_NICK_NAME_PREFIX + RandomUtil.randomString(10));
        save(user);
        return user;
    }
}
