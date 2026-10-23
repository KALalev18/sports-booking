// Shopping cart for the shop page. The cart is kept in localStorage so it survives logging in.
// At checkout only product ids and quantities are sent; the server looks up the prices itself.
const STORAGE_KEY = "sports-cart";
const MAX_QUANTITY = 99;

function loadCart() {
    try {
        return JSON.parse(localStorage.getItem(STORAGE_KEY)) || {};
    } catch (e) {
        return {};
    }
}

function saveCart(cart) {
    try {
        localStorage.setItem(STORAGE_KEY, JSON.stringify(cart));
    } catch (e) {
        // Storage can be unavailable (private mode); the cart then lasts for this page only
    }
}

let cart = loadCart();

const drawer = document.querySelector("[data-cart]");
const itemList = document.querySelector("[data-cart-items]");
const totalLabel = document.querySelector("[data-cart-total]");
const countLabel = document.querySelector("[data-cart-count]");
const checkoutForm = document.querySelector("[data-checkout-form]");

function formatPrice(value) {
    return value.toFixed(2) + " BGN";
}

function addToCart(button) {
    const id = button.dataset.id;
    if (cart[id]) {
        cart[id].quantity = Math.min(cart[id].quantity + 1, MAX_QUANTITY);
    } else {
        cart[id] = {
            name: button.dataset.name,
            price: Number(button.dataset.price),
            image: button.dataset.image,
            quantity: 1
        };
    }
    saveCart(cart);
    render();
    drawer.classList.add("open");
}

function changeQuantity(id, quantity) {
    if (quantity <= 0) {
        delete cart[id];
    } else {
        cart[id].quantity = Math.min(quantity, MAX_QUANTITY);
    }
    saveCart(cart);
    render();
}

function cartLine(id, item) {
    const li = document.createElement("li");

    const image = document.createElement("img");
    image.src = item.image;
    image.alt = "";

    const info = document.createElement("div");
    const name = document.createElement("div");
    name.textContent = item.name;
    const price = document.createElement("div");
    price.className = "muted";
    price.textContent = formatPrice(item.price * item.quantity);
    info.append(name, price);

    const quantity = document.createElement("div");
    quantity.className = "quantity";
    const minus = document.createElement("button");
    minus.type = "button";
    minus.textContent = "−";
    minus.setAttribute("aria-label", "Remove one " + item.name);
    minus.addEventListener("click", () => changeQuantity(id, item.quantity - 1));
    const count = document.createElement("span");
    count.textContent = item.quantity;
    const plus = document.createElement("button");
    plus.type = "button";
    plus.textContent = "+";
    plus.setAttribute("aria-label", "Add one " + item.name);
    plus.addEventListener("click", () => changeQuantity(id, item.quantity + 1));
    quantity.append(minus, count, plus);

    li.append(image, info, quantity);
    return li;
}

function render() {
    itemList.replaceChildren();
    let total = 0;
    let count = 0;
    const ids = Object.keys(cart);

    for (const id of ids) {
        const item = cart[id];
        total += item.price * item.quantity;
        count += item.quantity;
        itemList.append(cartLine(id, item));
    }
    if (ids.length === 0) {
        const empty = document.createElement("li");
        empty.className = "cart-empty";
        empty.textContent = "Your cart is empty.";
        itemList.append(empty);
    }

    totalLabel.textContent = formatPrice(total);
    countLabel.textContent = count;
    if (checkoutForm) {
        checkoutForm.querySelector("button").disabled = ids.length === 0;
    }
}

// Adds one productId and one quantity field per cart line to the checkout form
function fillCheckoutForm() {
    checkoutForm.querySelectorAll("input[name=productId], input[name=quantity]").forEach(input => input.remove());
    for (const [id, item] of Object.entries(cart)) {
        for (const [name, value] of [["productId", id], ["quantity", item.quantity]]) {
            const input = document.createElement("input");
            input.type = "hidden";
            input.name = name;
            input.value = value;
            checkoutForm.append(input);
        }
    }
}

document.querySelectorAll("[data-add-to-cart]").forEach(button => {
    button.addEventListener("click", () => addToCart(button));
});
document.querySelector("[data-cart-open]").addEventListener("click", () => drawer.classList.add("open"));
document.querySelector("[data-cart-close]").addEventListener("click", () => drawer.classList.remove("open"));
if (checkoutForm) {
    checkoutForm.addEventListener("submit", fillCheckoutForm);
}

render();
