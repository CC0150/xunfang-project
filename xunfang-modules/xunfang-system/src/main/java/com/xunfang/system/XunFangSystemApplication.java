package com.xunfang.system;

import com.xunfang.common.security.annotation.EnableRyFeignClients;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import com.xunfang.common.security.annotation.EnableCustomConfig;

/**
 * 系统模块
 *
 * @author xunfang
 */
@EnableCustomConfig
@EnableRyFeignClients
@SpringBootApplication
@ComponentScan(basePackages = {"com.xunfang.system", "com.xunfang.manufacture"})
public class XunFangSystemApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(XunFangSystemApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  系统模块启动成功   ლ(´ڡ`ლ)ﾞ  \n" +
                " .-------.       ____     __        \n" +
                " |  _ _   \\      \\   \\   /  /    \n" +
                " | ( ' )  |       \\  _. /  '       \n" +
                " |(_ o _) /        _( )_ .'         \n" +
                " | (_,_).' __  ___(_ o _)'          \n" +
                " |  |\\ \\  |  ||   |(_,_)'         \n" +
                " |  | \\ `'   /|   `-'  /           \n" +
                " |  |  \\    /  \\      /           \n" +
                " ''-'   `'-'    `-..-'              ");
    }
}
