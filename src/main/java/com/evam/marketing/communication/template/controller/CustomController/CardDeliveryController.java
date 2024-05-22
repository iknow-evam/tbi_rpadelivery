package com.evam.marketing.communication.template.controller.CustomController;

import com.evam.marketing.communication.template.controller.CustomController.request.CardDeliveryRequest;
import com.evam.marketing.communication.template.controller.health.HealthControllerMapping;
import com.evam.marketing.communication.template.service.client.WorkerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("CardDelivery/")
@RequiredArgsConstructor
public class CardDeliveryController {
    private final WorkerService workerService;
    @PostMapping("GetStatus")
    public void GetStatus(@RequestBody CardDeliveryRequest cardDeliveryRequest) {
        workerService.callService(cardDeliveryRequest);
    }
}
