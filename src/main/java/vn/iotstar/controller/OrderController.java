package vn.iotstar.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.iotstar.entity.Order;
import vn.iotstar.repository.OrderRepository;

import java.util.List;

@Controller
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderRepository orderRepository;

    // Xem danh sách đơn hàng (có hỗ trợ lọc theo trạng thái)
    @GetMapping
    public String listOrders(@RequestParam(value = "status", required = false) String status, Model model) {
        List<Order> orders;

        if (status != null && !status.trim().isEmpty() && !status.equalsIgnoreCase("ALL")) {
            orders = orderRepository.findByStatus(status.toUpperCase());
        } else {
            orders = orderRepository.findAll();
        }

        model.addAttribute("orders", orders);
        model.addAttribute("selectedStatus", status != null ? status : "ALL");

        return "orders/list";
    }

    // Xem chi tiết một đơn hàng
    @GetMapping("/{id}")
    public String orderDetail(@PathVariable("id") Long id, Model model) {
        Order order = orderRepository.findById(id).orElse(null);
        model.addAttribute("order", order);
        return "orders/detail";
    }
}