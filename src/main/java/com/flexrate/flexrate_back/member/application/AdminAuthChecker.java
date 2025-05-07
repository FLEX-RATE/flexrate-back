package com.flexrate.flexrate_back.member.application;

import java.security.Principal;

public interface AdminAuthChecker {
    boolean isAdmin(Principal principal);
}
