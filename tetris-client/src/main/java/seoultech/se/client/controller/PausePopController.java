package seoultech.se.client.controller;

import org.springframework.beans.factory.annotation.Autowired;

import seoultech.se.client.service.NavigationService;

public class PausePopController extends BaseController {

    @Autowired
    private NavigationService navigationService;
}
