package com.aihuishou.lhs.afl.doc;

import com.aihuishou.lhs.afl.doc.annotation.Biz;
import com.aihuishou.lhs.afl.doc.annotation.Scene;
import org.springframework.stereotype.Service;

/**
 * TODO
 *
 * @author eddieYang
 * @date 2023/4/25
 * @since
 */
@Service
public class UserServiceImpl implements  UserService{

    @Override
    @Scene(entryPoint = "更新用户", subject = User.class , caseW = "用户编辑")
    public void updateUser() {

    }

    @Override
    @Scene(entryPoint = "获取用户", subject = User.class)
    public void getUser() {

    }
}
