package com.snowflake.demo;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @title: IdGeneratorSnowflake
 * @Author Wen
 * @Date: 15/7/2021 10:07 AM
 * @Version 1.0
 */
@Component
public class IdGeneratorSnowflake {

    private long workerId = 0;
    private long datacenterId = 1;
    private Snowflake snowflake = IdUtil.getSnowflake();


    public synchronized long snowflakeId() {

        return snowflake.nextId();
    }

    public synchronized long snowflakeId(long workerId, long datacenterId) {
        Snowflake snowflake = IdUtil.getSnowflake(workerId, datacenterId);
        return snowflake.nextId();
    }

    public static void main(String[] args) {

        IdGeneratorSnowflake snowflake = new IdGeneratorSnowflake();

        ExecutorService service = Executors.newFixedThreadPool(5);
        for (int i = 0; i < 20; i++) {
            service.execute(
                    () -> {
                        System.out.println(snowflake.snowflakeId());

                    });
        }

        service.shutdown();

    }

}