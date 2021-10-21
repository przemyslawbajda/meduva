package com.szusta.meduva.controller;

import com.szusta.meduva.model.UnregisteredClient;
import com.szusta.meduva.payload.request.add.NewUnregisteredClient;
import com.szusta.meduva.service.UnregisteredClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/unregistered-client")
public class UnregisteredClientController {

    UnregisteredClientService clientService;

    @Autowired
    public UnregisteredClientController(UnregisteredClientService clientService) {
        this.clientService = clientService;
    }

    /*
    @GetMapping("/find/{id}")
    public UnregisteredClient getById(@PathVariable Long id) {
        return clientService.getUnregisteredClient(id);
    }
     */

    @GetMapping("/all")
    public List<UnregisteredClient> findAll() {
        return clientService.findAll();
    }

    @GetMapping("/all/undeleted")
    public List<UnregisteredClient> findAllUndeleted() {
        return clientService.findAllUndeleted();
    }

    @PostMapping("/add")
    public UnregisteredClient add(@RequestBody NewUnregisteredClient client){
        UnregisteredClient newClient = new UnregisteredClient();
        newClient.setName(client.getName());
        newClient.setSurname(client.getSurname());
        newClient.setPhoneNumber(client.getPhoneNumber());
        return clientService.save(newClient);
    }
}
