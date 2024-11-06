package GUI;

public interface PaymentPlugin {
    void openPaymentWindow(String categoria ,int cantidadAsientos,double precioTotal);
    void openPaymentSummaryWindow(String cardNumber);
    boolean validateCard(String cardNumber, String expiryDate, String cvv);
    boolean getStateSold();
}
