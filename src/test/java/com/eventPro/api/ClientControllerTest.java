package com.eventPro.api;

import com.eventPro.api.Controller.ClientController;
import com.eventPro.api.Dto.ClientRegisterRequest;
import com.eventPro.api.Dto.ClientResponse;
import com.eventPro.api.Service.ClientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(ClientController.class)
public class ClientControllerTest {

    @Autowired
    MockMvc mockMvc ;

    @MockitoBean
    ClientService clientService ;

    @Test
    public void controllerSaveClientTest()throws Exception{
        ClientResponse response = clientService.save(any()) ;
        mockMvc.perform(post("/api/v1/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {
                                "name":"lahcen",
                                "email":"lahcen@mail.com",
                                "phone":"0612345678",
                                "password":"Lahcen123"
                                }
                                """
                        ))
                        .andExpect(status().isCreated())
                        .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    public void theEmailIsNullTest()throws Exception{
        mockMvc.perform(post("/api/v1/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {
                                "name":"lahcen",
                                "email":"",
                                "phone":"0612345678",
                                "password":"Lahcen123"
                                }
                                """
                        ))
                .andExpect(status().isBadRequest()) ;
    }

    @Test
    public void passwordIsVerySmallTest()throws Exception{
        mockMvc.perform(post("/api/v1/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                """
                                {
                                "name":"lahcen",
                                "email":"lahcen@mail.com",
                                "phone":"0612345678",
                                "password":"Lah"
                                }
                                """
                        ))
                .andExpect(status().isBadRequest()) ;
    }

}
