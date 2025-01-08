package com.gudy.gateway;

import com.gudy.gateway.bean.GatewayConfig;
import com.gudy.gateway.thirdpart.checksum.ByteCheckSum;
import com.gudy.gateway.thirdpart.codec.BodyCodec;
import org.dom4j.DocumentException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GatewayApplication {

    public static void main(String[] args) {

/*        String configFileName = "gateway.xml";

        GatewayConfig gatewayConfig = new GatewayConfig();

        try {
            gatewayConfig.initAndParseConfig(GatewayConfig.class.getResource("/").getPath() + configFileName);

            gatewayConfig.setCs(new ByteCheckSum());

            gatewayConfig.setBodyCodec(new BodyCodec());

            gatewayConfig.startup();
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        }*/
        SpringApplication.run(GatewayApplication.class, args);
    }

}
