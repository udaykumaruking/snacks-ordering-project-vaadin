package com.gmail.udaykumar;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.converter.StringToIntegerConverter;
import com.vaadin.flow.data.validator.IntegerRangeValidator;
import com.vaadin.flow.router.Route;

import java.util.*;
import java.util.stream.Collectors;

/**
 * The main view contains a text field for getting the user name and a button
 * that shows a greeting message in a notification.
 */
@Route("")
@CssImport("./styles/shared-styles.css")
@CssImport(value = "./styles/vaadin-text-field-styles.css", themeFor = "vaadin-text-field")
public class MainView extends VerticalLayout {
    private Grid<SnackOrder> snackOrderGrid = new Grid<>(SnackOrder.class);
    private List<SnackOrder> snackOrders = new LinkedList<>();
    public MainView() {
        add(
                new H1("Snack Order"),
                buildForm(),
                snackOrderGrid
        );
    }
    private Component buildForm() {

        Map<String, List<String>> snacks = new HashMap<>();
        snacks.put("Fruits", Arrays.asList("Banana", "Apple", "Orange", "Avocado"));
        snacks.put("Candy", Arrays.asList("Chocolate bar", "Gummy bears", "Granola bar"));
        snacks.put("Drinks", Arrays.asList("Soda", "Water", "Coffee", "Tea"));
        TextField nameField = new TextField("Name");
        TextField quantityField = new TextField("Quantity");
        ComboBox<String> typeSelect = new ComboBox<>("Type", snacks.keySet());
        ComboBox<String> snackSelect = new ComboBox<>("Snack", Collections.emptyList());
        Button OrderButton = new Button("Order");
        Div errorsLayout = new Div();

        OrderButton.setThemeName("Primary");
        OrderButton.setEnabled(false);

            snackSelect.setEnabled(false);
            typeSelect.addValueChangeListener(e -> {
                String type = e.getValue();
                boolean enabled = type != null && !type.isEmpty();
                snackSelect.setEnabled(enabled);
                if (enabled) {
                    snackSelect.setValue("");
                    snackSelect.setItems(snacks.get(type));
                }
            });
        Binder<SnackOrder> binder = new Binder<>(SnackOrder.class);

        binder.forField(nameField)
                .asRequired("Name is Required")
                .bind("name");
        binder.forField(quantityField)
                .asRequired()
                .withConverter(new StringToIntegerConverter("Quantity needs to be a number"))
                .withValidator(new IntegerRangeValidator("Quantity needs to be atleast one", 1, 50))
                .bind("quantity");
        binder.forField(snackSelect)
                .asRequired("Please select a snack")
                .bind("snack");


        binder.addStatusChangeListener(status -> {
            OrderButton.setEnabled(!status.hasValidationErrors());
        });

        binder.readBean(new SnackOrder());

        OrderButton.addClickListener(click -> {

            try {
                errorsLayout.setText("");
                SnackOrder savedOrder = new SnackOrder();
                binder.writeBean(savedOrder);
                addOrder(savedOrder);
                binder.readBean(new SnackOrder());

                typeSelect.setValue("");
            } catch (ValidationException e) {
                errorsLayout.add(new Html(
                        e.getValidationErrors().stream().map(err ->
                                "<p>" + err.getErrorMessage() + "</p>")
                        .collect(Collectors.joining("\n"))
                ));
            }
        });


        HorizontalLayout formLayout = new HorizontalLayout(nameField, quantityField, typeSelect, snackSelect, OrderButton);
        Div wrappersLayout = new Div(formLayout, errorsLayout);
        formLayout.setDefaultVerticalComponentAlignment(Alignment.BASELINE);
        wrappersLayout.setWidth("100%");
        return wrappersLayout;
    }

    private void addOrder(SnackOrder savedOrder) {
        snackOrders.add(savedOrder);
        snackOrderGrid.setItems(snackOrders);
    }
}
