package com.example.foodorder.order;

import org.apache.activemq.broker.BrokerService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.annotation.EnableJms;

@SpringBootApplication
@EnableJms
public class OrderServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }

    /**
     * Programmatic creation of the embedded ActiveMQ Broker on localhost:61616.
     * This allows all microservices to use the standard tcp://localhost:61616 url.
     */
    @Bean(destroyMethod = "stop")
    public BrokerService activemqBroker() throws Exception {
        BrokerService broker = new BrokerService();
        broker.setBrokerName("online-food-mq");
        broker.addConnector("tcp://localhost:61616");
        broker.setPersistent(false);
        broker.setUseJmx(false);
        broker.start();
        System.out.println("ActiveMQ Embedded Broker started on tcp://localhost:61616");
        return broker;
    }
}
