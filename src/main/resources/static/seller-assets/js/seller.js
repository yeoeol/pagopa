(function () {
    "use strict";

    var THEME_KEY = "pagopa-seller-theme";

    function applyTheme(theme) {
        document.documentElement.setAttribute("data-bs-theme", theme);
        localStorage.setItem(THEME_KEY, theme);
    }

    function configureTheme() {
        var storedTheme = localStorage.getItem(THEME_KEY);
        var preferredTheme = window.matchMedia("(prefers-color-scheme: dark)").matches ? "dark" : "light";
        applyTheme(storedTheme || preferredTheme);

        var toggle = document.getElementById("theme-toggle");
        if (toggle) {
            toggle.addEventListener("click", function () {
                var current = document.documentElement.getAttribute("data-bs-theme");
                applyTheme(current === "dark" ? "light" : "dark");
            });
        }
    }

    function configureHtmx() {
        if (!window.htmx) {
            return;
        }

        document.body.addEventListener("htmx:responseError", function () {
            showNotice("요청을 처리하지 못했습니다. 잠시 후 다시 시도해 주세요.", "danger");
        });
    }

    function configureRegisterModal() {
        var modal = document.getElementById("product-register-modal");
        var form = document.getElementById("product-register-form");
        var list = document.getElementById("image-url-list");
        var addButton = document.getElementById("add-image-url");

        if (!modal || !form || !list || !addButton) {
            return;
        }

        if (!list.querySelector("[data-image-url]")) {
            addImageUrl(list, "");
        }
        reindexImageUrls(list);

        addButton.addEventListener("click", function () {
            addImageUrl(list, "");
        });

        list.addEventListener("click", function (event) {
            var button = event.target.closest("[data-remove-image-url]");
            if (!button || list.querySelectorAll(".seller-image-url-row").length === 1) {
                return;
            }
            button.closest(".seller-image-url-row").remove();
            reindexImageUrls(list);
        });

        modal.addEventListener("hidden.bs.modal", function () {
            form.reset();
            list.innerHTML = "";
            addImageUrl(list, "");
        });

        if (modal.dataset.openOnLoad === "true") {
            showModal(modal);
        }
    }

    function addImageUrl(list, value) {
        var row = document.createElement("div");
        row.className = "seller-image-url-row";
        row.innerHTML = '<input class="form-control" type="url" maxlength="512" required '
            + 'data-image-url placeholder="https://example.com/product.jpg" aria-label="상품 이미지 URL">'
            + '<button class="btn btn-icon btn-outline-danger seller-image-url-remove" type="button" '
            + 'data-remove-image-url aria-label="이미지 URL 삭제">×</button>';
        row.querySelector("input").value = value || "";
        list.appendChild(row);
        reindexImageUrls(list);
    }

    function reindexImageUrls(list) {
        var rows = list.querySelectorAll(".seller-image-url-row");
        rows.forEach(function (row, index) {
            row.querySelector("[data-image-url]").name = "imageUrls[" + index + "]";
            row.querySelector("[data-remove-image-url]").disabled = rows.length === 1;
        });
    }

    function configureDetailModal() {
        var modal = document.getElementById("product-detail-modal");
        if (!modal) {
            return;
        }

        modal.addEventListener("hidden.bs.modal", function () {
            document.getElementById("product-detail-content").innerHTML = '<div class="modal-body py-6 text-center">'
                + '<div class="spinner-border text-primary" role="status"></div>'
                + '<div class="text-secondary mt-3">상품 정보를 불러오는 중입니다.</div>'
                + '</div>';
        });
    }

    function configureLogout() {
        var button = document.getElementById("seller-logout");
        var page = document.getElementById("seller-products-page");
        if (!button || !page) {
            return;
        }

        button.addEventListener("click", async function () {
            button.disabled = true;
            try {
                var response = await fetch(page.dataset.logoutApi, {
                    method: "POST",
                    credentials: "same-origin",
                    headers: {Accept: "application/json"}
                });
                if (!response.ok) {
                    throw new Error();
                }
                window.location.assign("/");
            } catch (error) {
                showNotice("로그아웃하지 못했습니다. 잠시 후 다시 시도해 주세요.", "danger");
                button.disabled = false;
            }
        });
    }

    function showModal(element) {
        if (window.tabler && window.tabler.Modal) {
            window.tabler.Modal.getOrCreateInstance(element).show();
            return;
        }
        if (window.bootstrap && window.bootstrap.Modal) {
            window.bootstrap.Modal.getOrCreateInstance(element).show();
        }
    }

    function showNotice(message, variant) {
        var root = document.getElementById("seller-notice");
        var alert = document.createElement("div");
        alert.className = "alert alert-" + variant + " shadow-sm";
        alert.setAttribute("role", "alert");
        alert.textContent = message;
        root.replaceChildren(alert);
        window.setTimeout(function () {
            root.replaceChildren();
        }, 4000);
    }

    document.addEventListener("DOMContentLoaded", function () {
        configureTheme();
        configureHtmx();
        configureRegisterModal();
        configureDetailModal();
        configureLogout();
    });
})();
