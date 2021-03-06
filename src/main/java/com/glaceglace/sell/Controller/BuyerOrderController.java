package com.glaceglace.sell.Controller;

import com.glaceglace.sell.Converter.OrderForm2OrderDTOConverter;
import com.glaceglace.sell.DTO.OrderDTO;
import com.glaceglace.sell.Exceptions.SellException;
import com.glaceglace.sell.Form.OrderForm;
import com.glaceglace.sell.Service.BuyerService;
import com.glaceglace.sell.Service.OrderService;
import com.glaceglace.sell.Utils.ResultVOUtil;
import com.glaceglace.sell.VueObjects.ResultVO;
import com.glaceglace.sell.enums.ResultEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/buyer/order")
@Slf4j
public class BuyerOrderController {

    @Autowired
    private BuyerService buyerService;
    @Autowired
    private OrderService orderService;

    //Create order
    @PostMapping("/create")
    public ResultVO<Map<String, String>> create(@Valid OrderForm orderForm, BindingResult bindingResult){

        if(bindingResult.hasErrors()) {
            log.error("Create order: invalid arguments, orderForm = {}", orderForm);
            throw new SellException(ResultEnum.PARAM_ERROR.getCode(), bindingResult.getFieldError().getDefaultMessage());
        }
        OrderDTO orderDTO = OrderForm2OrderDTOConverter.convert(orderForm);
        if(CollectionUtils.isEmpty(orderDTO.getOrderDetailList())){
            log.error("Cart cant be empty");
            throw new SellException(ResultEnum.CART_EMPTY_ERROR);
        }
        OrderDTO createResult = orderService.create(orderDTO);
        Map<String, String> map = new HashMap<>();
        map.put("orderId", createResult.getOrderId());
        return ResultVOUtil.success(map);


    }

    //Order list
    @GetMapping("/list")
    public ResultVO<List<OrderDTO>> list(@RequestParam ("openid") String openid,
                                         @RequestParam(value = "page", defaultValue = "0") Integer page,
                                         @RequestParam(value = "size", defaultValue = "10") Integer size){
        if(StringUtils.isEmpty(openid)){
            log.error("openid is empty");
            throw new SellException(ResultEnum.PARAM_ERROR);
        }
        PageRequest request = new PageRequest(page, size);
        Page<OrderDTO> orderDTOPage = orderService.findList(openid, request);
        return ResultVOUtil.success(orderDTOPage.getContent());

    }

    //Order detail
    @GetMapping("/detail")
    public ResultVO<OrderDTO> detail(@RequestParam("openid") String openid,
                                     @RequestParam("orderId") String orderId){
        //TODO unsafe method, need to be make better
        OrderDTO orderDTO = buyerService.findOrderOne(openid,orderId);
        return ResultVOUtil.success(orderDTO);
    }

    //Cancel Order
    @PostMapping("/cancel")
    public ResultVO<OrderDTO> cancel(@RequestParam("openid") String openid,
                                     @RequestParam("orderId") String orderId){
        //TODO unsafe method, need to be make better

        buyerService.cancelOrder(openid,orderId);
        return ResultVOUtil.success();
    }

}
