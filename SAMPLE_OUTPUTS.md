# Sample Inputs and Calculated Outputs

Each sample input file lives in `samples/`. The outputs below were captured on
2026-10-06 with:

```
java -cp out store.ConsoleApp < samples/sampleN.txt
```

Because the input is piped, the terminal echoes prompts back-to-back on one
line (interactive use shows one prompt at a time; the answers typed are the
lines of the input file, in order).

---

## Sample 1 - normal order, promo + premium discount, Zone B

**Input (`samples/sample1.txt`):**

```
2
Jollof Rice Party Pack
15000
2
Bottle of Zobo Drink
500
4
premium
WELCOME10
B
```

**Output:**

```
======================================================
 Online Store - Order Pricing & Delivery Calculator
======================================================
How many different items are on the order? --- Item 1 of 2 ---
Item name: Unit price in NGN: Quantity: --- Item 2 of 2 ---
Item name: Unit price in NGN: Quantity: Customer type (standard / premium / student) [standard]: Promo code (press Enter for none): Delivery zone (A / B / C):
------------------- Order summary -------------------
  2 x Jollof Rice Party Pack   @   NGN 15,000.00 =   NGN 30,000.00
  4 x Bottle of Zobo Drink     @      NGN 500.00 =    NGN 2,000.00

 Subtotal:                      NGN 32,000.00
 Discount (15%):                -NGN 4,800.00
 Discounted subtotal:           NGN 27,200.00
 Delivery fee (Zone B - Lagos Mainland):    NGN 2,500.00
 VAT (7.50%):                    NGN 2,040.00
------------------------------------------------------
 TOTAL PAYABLE:                 NGN 31,740.00

Rules applied:
  * Promo code WELCOME10: 10% discount.
  * Premium member discount: 5%.
```

*Check:* 15% = WELCOME10 (10%) + premium (5%); 32,000 - 4,800 = 27,200;
VAT 7.5% of 27,200 = 2,040; 27,200 + 2,500 + 2,040 = **31,740.00**

---

## Sample 2 - large order, stacked discounts hit the 25% cap, Zone C

**Input (`samples/sample2.txt`):**

```
1
Smart TV 43 inch
180000
1
premium
VIP20
C
```

**Output:**

```
======================================================
 Online Store - Order Pricing & Delivery Calculator
======================================================
How many different items are on the order? --- Item 1 of 1 ---
Item name: Unit price in NGN: Quantity: Customer type (standard / premium / student) [standard]: Promo code (press Enter for none): Delivery zone (A / B / C):
------------------- Order summary -------------------
  1 x Smart TV 43 inch         @  NGN 180,000.00 =  NGN 180,000.00

 Subtotal:                     NGN 180,000.00
 Discount (25%):               -NGN 45,000.00
 Discounted subtotal:          NGN 135,000.00
 Delivery fee (Zone C - Outside Lagos):    NGN 5,000.00
 VAT (7.50%):                   NGN 10,125.00
------------------------------------------------------
 TOTAL PAYABLE:                NGN 150,125.00

Rules applied:
  * Order-value discount: 10% (subtotal of NGN 180,000.00 is above NGN 100,000.00).
  * Promo code VIP20: 20% discount.
  * Premium member discount: 5%.
  * Combined discounts capped at 25%.
```

*Check:* 10% + 20% + 5% = 35% -> capped at 25%; 180,000 - 45,000 = 135,000
(below the 150,000 free-delivery threshold, so the Zone C fee applies);
VAT = 10,125; total = 135,000 + 5,000 + 10,125 = **150,125.00**

---

## Sample 3 - free delivery triggered by order value

**Input (`samples/sample3.txt`):**

```
1
Laptop
170000
1
standard

A
```

**Output:**

```
======================================================
 Online Store - Order Pricing & Delivery Calculator
======================================================
How many different items are on the order? --- Item 1 of 1 ---
Item name: Unit price in NGN: Quantity: Customer type (standard / premium / student) [standard]: Promo code (press Enter for none): Delivery zone (A / B / C):
------------------- Order summary -------------------
  1 x Laptop                   @  NGN 170,000.00 =  NGN 170,000.00

 Subtotal:                     NGN 170,000.00
 Discount (10%):               -NGN 17,000.00
 Discounted subtotal:          NGN 153,000.00
 Delivery fee (Zone A - Lagos Island):            FREE
 VAT (7.50%):                   NGN 11,475.00
------------------------------------------------------
 TOTAL PAYABLE:                NGN 164,475.00

Rules applied:
  * Order-value discount: 10% (subtotal of NGN 170,000.00 is above NGN 100,000.00).
  * Free delivery: discounted order value of NGN 153,000.00 is above NGN 150,000.00.
```

*Check:* 170,000 - 17,000 = 153,000 >= 150,000 -> delivery FREE;
VAT = 11,475; total = 153,000 + 0 + 11,475 = **164,475.00**

---

## Sample 4 - invalid values are rejected with clear messages, unknown promo ignored

**Input (`samples/sample4.txt`):**

```
0
2
Rice
-50
12000
0
2
Beans
abc
8000
3
vip
standard
SAVE50
Z
A
```

**Output:**

```
======================================================
 Online Store - Order Pricing & Delivery Calculator
======================================================
How many different items are on the order? Error: Number of items must be between 1 and 50, got 0.
How many different items are on the order? --- Item 1 of 2 ---
Item name: Unit price in NGN: Error: Price must be a positive amount, got -50.
Unit price in NGN: Quantity: Error: Quantity must be between 1 and 100, got 0.
Quantity: --- Item 2 of 2 ---
Item name: Unit price in NGN: Error: Price must be a number, got 'abc'.
Unit price in NGN: Quantity: Customer type (standard / premium / student) [standard]: Error: Unknown customer type 'vip'. Valid types: standard, premium or student.
Customer type (standard / premium / student) [standard]: Promo code (press Enter for none): Delivery zone (A / B / C): Error: Unknown delivery zone 'Z'. Valid zones: A, B or C.
Delivery zone (A / B / C):
------------------- Order summary -------------------
  2 x Rice                     @   NGN 12,000.00 =   NGN 24,000.00
  3 x Beans                    @    NGN 8,000.00 =   NGN 24,000.00

 Subtotal:                      NGN 48,000.00
 Discounted subtotal:           NGN 48,000.00
 Delivery fee (Zone A - Lagos Island):    NGN 1,500.00
 VAT (7.50%):                    NGN 3,600.00
------------------------------------------------------
 TOTAL PAYABLE:                 NGN 53,100.00

Rules applied:
  * Promo code 'SAVE50' not recognised - ignored.
```

*Check:* every invalid line prints a clear `Error:` message and re-prompts;
48,000 subtotal (below the 50,000 tier, no discounts); VAT = 3,600;
total = 48,000 + 1,500 + 3,600 = **53,100.00**
