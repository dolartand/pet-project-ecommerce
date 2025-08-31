import React from "react";
import {Navigate, Outlet} from "react-router-dom";
import {useAuth} from "../context/AuthContext";

const ProtectedRoute = () => {
    const {isLoggedIn} = useAuth();

    return isLoggedIn ? <Outlet /> : <Navigate to='/' replace />;
};

export default ProtectedRoute;