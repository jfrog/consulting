package com.jfrog.demogradle.services;


import com.fasterxml.jackson.databind.JsonNode;
import com.maxmind.db.Reader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;

@Service
public class GeoService {

    private InputStream database;

    @PostConstruct
    public void init() throws IOException {
       database = new ClassPathResource("GeoLite2-Country.mmdb").getInputStream();
    }


    public JsonNode getIpDetails(String ip) throws Exception {
       Reader reader = new Reader(database);
       JsonNode response = reader.get(InetAddress.getByName(ip));
       reader.close();

       return response;
   }
}
