package xyz.rgbsec.views.logout;

import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import xyz.rgbsec.views.main.MainView;

@Route(value = "logout", layout = MainView.class)
@PageTitle("Logout")
@CssImport("styles/views/logout/logout-view.css")
public class LogoutView extends Div {

    public LogoutView() {
        setId("logout-view");
        add(new Label("Content placeholder"));
    }

}
