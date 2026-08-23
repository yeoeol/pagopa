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

    document.addEventListener("DOMContentLoaded", function () {
        configureTheme();
        configureHtmx();
        configureModal();
        configureRolePopovers(document);
    });
})();
