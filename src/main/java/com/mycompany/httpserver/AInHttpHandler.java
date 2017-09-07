package com.mycompany.httpserver;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import com.mycompany.ffserver.FFRequest;
import com.mycompany.ffserver.FFRequest.EnumRequestType;
import com.mycompany.main.MainApplication;
import com.mycompany.modbus.ModbusRTU;
import com.mycompany.utils.JSONEncoder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import io.netty.buffer.Unpooled;

public class AInHttpHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange arg0) throws IOException {
        FFHttpServer.logger.debug(String.format("AInHttpHandler: %s", arg0.getRequestURI().toString()));

        // parse request
        Map<String, Object> parameters = new HashMap<String, Object>();
        String query = arg0.getRequestURI().getRawQuery();
        Utils.parseQuery(query, parameters);

        String response = "";
        String msg = "";
        Boolean result = false;
        int ai_state[] = new int[8];

        Object reg_str = parameters.get("reg_str");
        if (reg_str != null) {

            FFRequest req = new FFRequest((String) reg_str,
                    EnumRequestType.Modbus_0x04,
                    Unpooled.copiedBuffer(ModbusRTU.GenMsg_GetAI((byte) 0x01, 0x00, 0x08)),
                    5000);
            MainApplication.ff_server.addReqToQueue(req);

            while (!req.have_response
                    && (req.request_create_time + req.request_time_out_ms + 5000) > System.currentTimeMillis()) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

            if (req.have_response) {
                if (!req.result) {
                    msg = req.response.toString(Charset.defaultCharset());
                } else if (req.response.readableBytes() >= 37) {
                    req.response.readBytes(3);
                    for (int i = 0; i < 8; i++) {
                        ai_state[i] = req.response.readInt();
                    }
                    msg = "Success";
                    result = true;
                } else {
                    msg = "Error";
                }
            } else {
                msg = "Timeout";
            }
        } else {
            msg = "Request Params Error";
        }

        response = JSONEncoder.genAInResponse((String) reg_str, result, ai_state, msg);
        FFHttpServer.logger.debug(String.format("AInHttpHandler: %s", response));

        Utils.sendResponse(arg0, response);
    }
}
