(function () {
    "use strict";

    var THEME_KEY = "pagopa-admin-theme";

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

        document.body.addEventListener("htmx:configRequest", function (event) {
            var token = document.querySelector('meta[name="_csrf"]');
            var header = document.querySelector('meta[name="_csrf_header"]');
            if (token && header) {
                event.detail.headers[header.content] = token.content;
            }
        });

        document.body.addEventListener("htmx:beforeSwap", function (event) {
            var responseUrl = event.detail.xhr.responseURL || "";
            if (responseUrl.indexOf("/admin/login") !== -1) {
                window.location.assign("/admin/login");
            }
        });

        document.body.addEventListener("htmx:afterSwap", function (event) {
            configureRolePopovers(document);
        });

        document.body.addEventListener("htmx:afterRequest", function (event) {
            var requestConfig = event.detail.requestConfig;

            var trigger = requestConfig && requestConfig.elt
                ? requestConfig.elt
                : event.detail.elt;

            if (!trigger
                || !trigger.matches
                || !trigger.matches("[data-user-status-action]")) {
                return;
            }

            if (!event.detail.successful) {
                return;
            }

            showNotice(
                trigger.dataset.successMessage || "회원 상태를 변경했습니다.",
                "success"
            );

            refreshUserResults();
        });

        document.body.addEventListener("htmx:afterRequest", function (event) {
            var requestConfig = event.detail.requestConfig;

            var trigger = requestConfig && requestConfig.elt
                ? requestConfig.elt
                : event.detail.elt;

            if (!trigger
                || !trigger.matches
                || !trigger.matches("[data-seller-action]")) {
                return;
            }

            if (!event.detail.successful) {
                return;
            }

            if (trigger.hasAttribute("data-dismiss-seller-modal")) {
                hideSellerRejectModal();
            }

            showNotice(
                trigger.dataset.successMessage || "판매자 승급 요청을 처리했습니다.",
                "success"
            );

            refreshSellerResults();
        });

        document.body.addEventListener("htmx:responseError", function () {
            showNotice("요청을 처리하지 못했습니다. 잠시 후 다시 시도해 주세요.", "danger");
        });
    }

    function configureModal() {
        var modal = document.getElementById("user-detail-modal");
        if (!modal) {
            return;
        }

        modal.addEventListener("hidden.bs.modal", function () {
            var content = document.getElementById("user-detail-content");
            if (content) {
                content.innerHTML = '<div class="modal-body py-5 text-center">'
                    + '<div class="spinner-border text-primary" role="status"></div>'
                    + '<div class="text-secondary mt-3">회원 정보를 불러오는 중입니다.</div>'
                    + '</div>';
            }
        });
    }

    function configureSellerRejectModal() {
        var modal = document.getElementById("seller-reject-modal");
        var form = document.getElementById("seller-reject-form");

        if (!modal || !form) {
            return;
        }

        modal.addEventListener("show.bs.modal", function (event) {
            var trigger = event.relatedTarget;
            if (!trigger) {
                return;
            }

            var rejectUrl = trigger.dataset.rejectUrl;
            var sellerId = trigger.dataset.sellerId || "-";
            var sellerName = trigger.dataset.sellerName || "선택한 회원";

            document.getElementById("seller-reject-id").textContent = sellerId;
            document.getElementById("seller-reject-name").textContent = sellerName;

            form.setAttribute("action", rejectUrl);
            form.setAttribute("hx-post", rejectUrl);

            if (window.htmx) {
                window.htmx.process(form);
            }
        });

        modal.addEventListener("hidden.bs.modal", function () {
            form.reset();
            form.removeAttribute("action");
            form.removeAttribute("hx-post");
        });
    }

    function hideSellerRejectModal() {
        var modal = document.getElementById("seller-reject-modal");
        if (!modal) {
            return;
        }

        if (window.tabler && window.tabler.Modal) {
            window.tabler.Modal.getOrCreateInstance(modal).hide();
            return;
        }

        var closeButton = modal.querySelector("[data-bs-dismiss='modal']");
        if (closeButton) {
            closeButton.click();
        }
    }

    function showNotice(message, variant) {
        var root = document.getElementById("admin-notice");
        if (!root) {
            return;
        }

        root.innerHTML = '<div class="alert alert-' + variant + ' shadow-sm" role="alert">'
            + message
            + '</div>';
        window.setTimeout(function () {
            root.innerHTML = "";
        }, 4000);
    }

    function configureRolePopovers(root) {
        if (!window.tabler || !window.tabler.Popover) {
            return;
        }

        var scope = root || document;
        var roleBadges = scope.querySelectorAll("[data-role-popover]");

        roleBadges.forEach(function (roleBadge) {
            window.tabler.Popover.getOrCreateInstance(roleBadge, {
                container: "body",
                trigger: "focus",
                placement: "auto",
                html: false
            });
        });
    }

    function configureCategoryPage() {
        var page = document.getElementById("category-admin-page");
        if (!page) {
            return;
        }

        function syncActiveCategory() {
            var detail = document.getElementById("category-detail-panel");
            var selectedId = detail
                ? detail.dataset.selectedCategoryId || ""
                : "";

            page.querySelectorAll("[data-category-tree-link]").forEach(function (link) {
                var active = link.dataset.categoryId === selectedId;
                link.classList.toggle("active", active);
                if (active) {
                    link.setAttribute("aria-current", "page");
                } else {
                    link.removeAttribute("aria-current");
                }
            });
        }

        document.body.addEventListener("htmx:afterSwap", syncActiveCategory);
        document.body.addEventListener("htmx:historyRestore", syncActiveCategory);

        var modal = document.getElementById("category-create-modal");
        var form = document.getElementById("category-create-form");
        var parentSelect = document.getElementById("category-parent");
        var nameInput = document.getElementById("category-name");

        modal.addEventListener("show.bs.modal", function (event) {
            var trigger = event.relatedTarget;
            parentSelect.value = trigger
                ? trigger.dataset.categoryParentId || ""
                : "";
        });

        modal.addEventListener("shown.bs.modal", function () {
            nameInput.focus();
        });

        modal.addEventListener("hidden.bs.modal", function () {
            form.reset();
            nameInput.setCustomValidity("");
        });

        form.addEventListener("submit", function (event) {
            nameInput.value = nameInput.value.trim();
            if (nameInput.value) {
                return;
            }

            event.preventDefault();
            nameInput.setCustomValidity("카테고리명을 입력해 주세요.");
            nameInput.reportValidity();
        });

        nameInput.addEventListener("input", function () {
            nameInput.setCustomValidity("");
        });

        syncActiveCategory();
    }

    function refreshUserResults() {
        var userResults = document.getElementById("user-results");

        if (!userResults || !window.htmx) {
            return;
        }

        var listUrl = window.location.pathname + window.location.search;

        window.htmx.ajax("GET", listUrl, {
            target: "#user-results",
            swap: "outerHTML"
        });
    }

    function refreshSellerResults() {
        var sellerResults = document.getElementById("seller-results");

        if (!sellerResults || !window.htmx) {
            return;
        }

        var listUrl = window.location.pathname + window.location.search;

        window.htmx.ajax("GET", listUrl, {
            target: "#seller-results",
            swap: "outerHTML"
        });
    }

    document.addEventListener("DOMContentLoaded", function () {
        configureTheme();
        configureHtmx();
        configureModal();
        configureSellerRejectModal();
        configureRolePopovers(document);
        configureCategoryPage();
    });
})();
