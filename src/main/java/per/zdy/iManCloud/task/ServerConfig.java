package per.zdy.iManCloud.task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import per.zdy.iManCloud.domain.dao.SystemDao;

import java.util.Date;

/**
 * 初始化配置
 * */
@Component
@Order(value = 1)
public class ServerConfig implements ApplicationRunner {

    @Autowired
    SystemDao systemDao;


    @Override
    public void run(ApplicationArguments args) throws Exception {
        System.out.println("》》》》》》》》》》》");
    }

}
