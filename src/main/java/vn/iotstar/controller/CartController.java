package vn.iotstar.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;
import vn.iotstar.entity.Order;
import vn.iotstar.entity.OrderItem;
import vn.iotstar.service.OrderService;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private OrderService orderService;

    // Lấy giỏ hàng từ Session (nếu chưa có thì tạo mới)
    @SuppressWarnings("unchecked")
    private List<OrderItem> getCart(HttpSession session) {
        List<OrderItem> cart = (List<OrderItem>) session.getAttribute("cart");
        if (cart == null) {
            cart = new ArrayList<>();
            session.setAttribute("cart", cart);
        }
        return cart;
    }

    // 1. Màn hình Xem Giỏ hàng
    @GetMapping
    public String viewCart(Model model, HttpSession session) {
        List<OrderItem> cart = getCart(session);
        double totalAmount = cart.stream()
                .mapToDouble(item -> (item.getPrice() != null ? item.getPrice() : 0.0) * item.getQuantity())
                .sum();

        model.addAttribute("cartItems", cart);
        model.addAttribute("totalAmount", totalAmount);
        return "cart/list";
    }

    // 2. Thêm sản phẩm vào giỏ hàng (Hỗ trợ cả POST từ Form và GET từ Link)
    @RequestMapping(value = "/add")
    public String addToCart(@RequestParam(value = "productId", required = false, defaultValue = "1") Long productId,
                            @RequestParam(value = "productName", required = false, defaultValue = "Sản phẩm") String productName,
                            @RequestParam(value = "price", required = false, defaultValue = "100000") Double price,
                            @RequestParam(value = "quantity", required = false, defaultValue = "1") Integer quantity,
                            HttpSession session) {
        List<OrderItem> cart = getCart(session);
        boolean exists = false;

        for (OrderItem item : cart) {
            if (item.getProductId() != null && item.getProductId().equals(productId)) {
                int newQty = item.getQuantity() + quantity;
                // Giới hạn số lượng tối đa là 99
                item.setQuantity(Math.min(newQty, 99));
                exists = true;
                break;
            }
        }

        if (!exists) {
            OrderItem item = new OrderItem();
            item.setProductId(productId);
            item.setProductName(productName);
            item.setPrice(price);
            item.setQuantity(Math.min(Math.max(quantity, 1), 99));
            cart.add(item);
        }

        return "redirect:/cart";
    }

    // 3. Cập nhật số lượng (Thay đổi trong giới hạn 1 - 99)
    @PostMapping("/update")
    public String updateQuantity(@RequestParam("productId") Long productId,
                                 @RequestParam("quantity") Integer quantity,
                                 HttpSession session) {
        List<OrderItem> cart = getCart(session);

        // Kiểm tra giới hạn số lượng từ 1 đến 99
        if (quantity < 1) quantity = 1;
        if (quantity > 99) quantity = 99;

        for (OrderItem item : cart) {
            if (item.getProductId() != null && item.getProductId().equals(productId)) {
                item.setQuantity(quantity);
                break;
            }
        }
        return "redirect:/cart";
    }

    // 4. Xóa sản phẩm khỏi giỏ hàng
    @PostMapping("/remove")
    public String removeFromCart(@RequestParam("productId") Long productId, HttpSession session) {
        List<OrderItem> cart = getCart(session);
        cart.removeIf(item -> item.getProductId() != null && item.getProductId().equals(productId));
        return "redirect:/cart";
    }

    // 5. Hiển thị trang thanh toán (Checkout)
    @GetMapping("/checkout")
    public String checkoutForm(Model model, HttpSession session) {
        List<OrderItem> cart = getCart(session);
        if (cart.isEmpty()) {
            return "redirect:/cart";
        }

        double totalAmount = cart.stream()
                .mapToDouble(item -> (item.getPrice() != null ? item.getPrice() : 0.0) * item.getQuantity())
                .sum();

        Order order = new Order();
        order.setTotalAmount(totalAmount);

        model.addAttribute("order", order);
        model.addAttribute("cartItems", cart);
        return "cart/checkout";
    }

    // 6. Xử lý khi nhấn nút Xác Nhận Đặt Hàng (COD)
    @PostMapping("/checkout")
    public String processCheckout(@ModelAttribute("order") Order order, HttpSession session) {
        List<OrderItem> cart = getCart(session);

        if (!cart.isEmpty()) {
            double totalAmount = cart.stream()
                    .mapToDouble(item -> (item.getPrice() != null ? item.getPrice() : 0.0) * item.getQuantity())
                    .sum();

            order.setTotalAmount(totalAmount);
            order.setStatus("NEW"); // Trạng thái mặc định ban đầu
            order.setItems(new ArrayList<>(cart));

            orderService.createOrder(order);

            // Xóa sạch giỏ hàng sau khi đặt thành công
            session.removeAttribute("cart");
        }

        return "redirect:/orders";
    }
}