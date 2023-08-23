package com.tictactoe;

import java.io.*;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import java.io.IOException;
import java.util.List;


@WebServlet(name = "LogicServlet", value = "/logic")
public class LogicServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession currentSession = req.getSession();

        // Получаем объект игрового поля из сессии
        Field field = extractField(currentSession);

        // получаем индекс ячейки, по которой произошел клик
        int index = getSelectedIndex(req);
        Sign currentSign = field.getField().get(index);
        if (Sign.EMPTY != currentSign) {
            RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/index.jsp");
            dispatcher.forward(req, resp);
            return;
        }

        // ставим крестик в ячейке, по которой кликнул пользователь
        field.getField().put(index, Sign.CROSS);

        if (checkWin(resp, currentSession, field)) {
            return;
        }

        int emptyFieldIndex = field.getEmptyFieldIndex();

        if (emptyFieldIndex >= 0) {
            field.getField().put(emptyFieldIndex, Sign.NOUGHT);
            if (checkWin(resp, currentSession, field))
                return;
        }
        // Если пустой ячейки нет и никто не победил - значит это ничья
        else {
            // Добавляем в сессию флаг, который сигнализирует что произошла ничья
            currentSession.setAttribute("draw", true);
        }

        // Считаем список значков
        List<Sign> data = field.getFieldData();

        // Обновляем объект поля и список значков в сессии
        currentSession.setAttribute("data", data);
        currentSession.setAttribute("field", field);

        resp.sendRedirect("/index.jsp");
    }

    private boolean checkWin(HttpServletResponse response, HttpSession currentSession, Field field) throws IOException {
        Sign winner = field.checkWin();
        if (Sign.CROSS == winner || Sign.NOUGHT == winner) {
            // Добавляем флаг, который показывает что кто-то победил
            currentSession.setAttribute("winner", winner);

            // Считаем список значков
            List<Sign> data = field.getFieldData();

            // Обновляем этот список в сессии
            currentSession.setAttribute("data", data);

            // Шлем редирект
            response.sendRedirect("/index.jsp");
            return true;
        }
        return false;
    }



    private Field extractField(HttpSession currentSession) {
        Object fieldAttribute = currentSession.getAttribute("field");
        if (Field.class != fieldAttribute.getClass()) {
            currentSession.invalidate();
            throw new RuntimeException("Session is broken, try one more time");
        }
        return (Field) fieldAttribute;
    }

    private int getSelectedIndex(HttpServletRequest request) {
        String click = request.getParameter("click");
        boolean isNumeric = click.chars().allMatch(Character::isDigit);
        return isNumeric ? Integer.parseInt(click) : 0;
    }

}