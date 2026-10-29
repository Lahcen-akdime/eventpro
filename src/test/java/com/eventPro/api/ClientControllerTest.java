package com.eventPro.api;

import com.eventPro.api.Controller.Client.ClientController;
import com.eventPro.api.Dto.Client.ClientRegisterRequest;
import com.eventPro.api.Dto.Client.ClientResponse;
import com.eventPro.api.Service.ClientService;
import com.eventPro.api.Service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(ClientController.class)
public class ClientControllerTest {

    @Autowired
    MockMvc mockMvc ;

    @MockitoBean
    ClientService clientService ;

    @MockitoBean
    UserService userService ;

    @Test
    public void controllerSaveClientTest()throws Exception{
        ClientResponse response = ClientResponse.builder().name("test").build();
        when(clientService.save(any(ClientRegisterRequest.class))).thenReturn(response);
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
