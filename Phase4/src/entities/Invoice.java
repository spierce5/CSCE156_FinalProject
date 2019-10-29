package entities;

import java.time.LocalDate;
import java.util.ArrayList;

public class Invoice {
	private String invoiceCode;
	private Customer customer;
	private Person landlord;
	private LocalDate date;
	private ArrayList<Product> products;
	private double fees;
	private boolean lowIncome = false;
	private boolean housingCredit = false;
	

	
	public Invoice(String invoiceCode, Customer customer, Person landlord, LocalDate date, ArrayList<Product> products) {
		super();
		this.invoiceCode = invoiceCode;
		this.customer = customer;
		this.landlord = landlord;
		this.date = date;
		this.products = products;
	}
/*
 * Getter and Setter Methods 
 */
	public String getInvoiceCode() {
		return invoiceCode;
	}
	
	public void setInvoiceCode(String invoiceCode) {
		this.invoiceCode = invoiceCode;
	}
	
	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public Customer getCustomer() {
		return customer;
	}
	
	public void setCustomer(Customer customer) {
		this.customer = customer;
	}
	
	public Person getLandlord() {
		return landlord;
	}
	
	public void setLandlord(Person landlord) {
		this.landlord = landlord;
	}
	
	public ArrayList<Product> getProducts() {
		return products;
	}

	public void setProducts(ArrayList<Product> products) {
		this.products = products;
	}
	public double getFees() {
		return fees;
	}
	public double setFees(double fees) {	
		return fees;
	}
		
/*
 * Returns the sum of subtotals from all products on invoice
 */
	public double getSubtotal(ArrayList<Product> products) {
		double subtotal = 0;
		for(Product p: products) {
			subtotal += p.calculateSubtotal(customer, date);
		}
		return subtotal;
	}
	
	
/*
 * Calculates tax based on income level	
 */
	public double getTax(ArrayList<Product> products) {
			double totalTax = 0;
			for(Product p: products) {
				totalTax += p.calculateTax(customer, date);
			}
			return totalTax;
	}
/*
 * Calculates total after taxes and discount
 */
	public double getTotal(ArrayList<Product> products, Customer customer) {
		double Total = 0;
		for(Product p: products) {
			Total += p.calculateTotalCost(customer, date);
		}
		if(lowIncome) {
			Total *= 0.9;
			Total += 50.75;
			if(housingCredit) {
				Total -= 1000;
			}
		}
		return Total;
	}
/*
 * Calculates total discount
 */
	public double getDiscount(ArrayList<Product> products, boolean incomeDiscount, boolean housingDiscount) {
		double discount = 0;
		if(incomeDiscount) {
			discount = getTotal(products, customer) * 0.1;
			if(housingDiscount) {
				discount += 1000;
			}
		}
		return discount * -1.0;
	}
	
	
/*
 * Takes an array of invoices and prints a summary 
 */
	public void PrintSummary(ArrayList<Invoice> list) {
		System.out.println(String.format("%-10s %-30s %-20s %-16s %-12s %-12s %-12s %-12s", "Invoice",
				"Customer", "Realtor", "Subtotal", "Fees", "Tax", "Discount", "Total"));
		for(Invoice Inv: list) {
			if(Inv.getCustomer() instanceof LowIncome) 		{	//Printable version of customer type
				fees = 50.75;
				lowIncome = true;
				for(Product p: Inv.getProducts()) {
					if(p instanceof SaleAgreements || p instanceof LeaseAgreements) {
						housingCredit = true;
					}
				}
			}
			else {
				fees = 0.0;
			}
			
			String discount = String.format("%.2f", Inv.getDiscount(Inv.getProducts(), lowIncome, housingCredit));
			System.out.println(String.format("%-10s %-30s %-20s $%-16.2f %-12s $%-12.2f %-12s $%-12.2f", Inv.getInvoiceCode(),
					Inv.getCustomer().getName(), Inv.getLandlord().getFullName(), Inv.getSubtotal(Inv.getProducts()),
					fees, Inv.getTax(Inv.getProducts()), discount , Inv.getTotal(Inv.getProducts(),Inv.getCustomer())));
		}
	}
		
/*
 * Takes an array of invoices and prints out the individual invoices
 */
	public void PrintInvoices(ArrayList<Invoice> list) {
		System.out.println("Individual Invoice Details Report");
		System.out.println("==================================================");
		for(Invoice Inv: list) {
			String customerType = null;
			housingCredit = false;
			lowIncome = false;
			if(Inv.getCustomer() instanceof LowIncome) {			//Printable version of customer type
				customerType = "[Low-Income]";
				lowIncome = true;
			}
			else {
				customerType = "[General]";
			}
			System.out.println("Invoice" + Inv.getInvoiceCode());
			System.out.println("========================");					//General Information
			System.out.println("Realtor: " + Inv.getLandlord().getFullName());
			System.out.println("Customer Info:\n " + Inv.getCustomer().getName() + "\n " +
			customerType + "\n " + Inv.getCustomer().getContact().getFullName() + "\n " + 
			Inv.getCustomer().getAddress().getStreet() + "\n " + Inv.getCustomer().getAddress().getCity() +
			", " + Inv.getCustomer().getAddress().getState() + ", " + Inv.getCustomer().getAddress().getZipCode() + 
			" " +  Inv.getCustomer().getAddress().getCountry());
			System.out.println("-------------------------------------------");		//Itemized Report
			System.out.println(String.format("%-12s %-60s %-15s %-15s %-15s", "Code", "Item", "Subtotal", "Tax", "Total"));
			for(Product p: Inv.getProducts()) {
				StringBuilder item = new StringBuilder();
				StringBuilder item2 = new StringBuilder();
				switch(p.getProductType()) {
				case "S":
					SaleAgreements sa = (SaleAgreements) p;
					if(lowIncome) {
						housingCredit = true;
					}
					String interest = String.format("%.2f", sa.calculateInterest(Inv.getDate()));
					item.append("Sale Agreement @ " + sa.getAddress().getStreet());
					item2.append(sa.getQuantity() + " Units @ $" + sa.getMonthlyPayment() + " monthly, $" +  interest + " interest/unit");
					break;
				case "L":
					LeaseAgreements la = (LeaseAgreements) p;
					if(lowIncome) {
						housingCredit = true;
					}
					String price = String.format("%.2f", la.getPricePerApartment());
					item.append("Lease Agreement @ " + la.getAddress().getStreet());
					item2.append(la.getStartDate() + " (" + la.getQuantity() + " Units @ $" + price + "/Unit)");
					break;
				case "P":
					ParkingPass pk = (ParkingPass) p;
					String parking = String.format("%.2f", pk.getParkingFee());
					if(pk.getApartmentCode() == null){ 
						item.append("Parking Pass " + " (" + pk.getQuantity() + " Units @ $" + parking + ")");
					}
					else {
						item.append("Parking Pass " + pk.getApartmentCode().getProductCode() + " (" + pk.getQuantity() + " Units @ $"+ parking + " with " +
							(int) (pk.calculateDiscount(customer, date) / 55) + " free)");
					}
					break;
				case "A":
					Amenity am = (Amenity) p;
					String unitPrice = String.format("%.2f", am.getPrice());
					item.append(am.getName() + " (" + am.getQuantity() + " Units @ $" + unitPrice + " /Unit)");
					break;
				}
				System.out.println(String.format("%-12s %-60s %-15.2f %-15.2f %-15.2f", p.getProductCode(),
						item, p.calculateSubtotal(Inv.getCustomer(), Inv.getDate()), p.calculateTax(Inv.getCustomer(), Inv.getDate()), p.calculateTotalCost(Inv.getCustomer(), Inv.getDate())));
				if(item2 != null);{
					System.out.println(String.format("%-12s %-60s", " ", item2));
				}
			}
			System.out.println(String.format("%-72s %-45s", " ", "======================================"));
			System.out.println(String.format("%-72s %-15.2f %-15.2f %-15.2f", "SUBTOTALS", Inv.getSubtotal(Inv.getProducts()), Inv.getTax(Inv.getProducts()), Inv.getTotal(Inv.getProducts(), Inv.getCustomer())));
			if(housingCredit) {
				System.out.println(String.format("%-105s %-15.2f", "DISCOUNT (10% LOW INCOME + $1000 HOUSING CREDIT)", Inv.getDiscount(Inv.getProducts(), lowIncome, housingCredit)));
			}
			else if(lowIncome) {
				System.out.println(String.format("%-105s %-15.2f", "DISCOUNT (10% LOW INCOME)", Inv.getDiscount(Inv.getProducts(), lowIncome, housingCredit)));
			}
			if(lowIncome) {
				System.out.println(String.format("%-105s %-15.2f", "Additional Fee (Low-Income)", 50.75));
			}	
		}	
	}
}