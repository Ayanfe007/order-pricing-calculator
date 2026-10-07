# Design-Decision Notes (Week 2 - domain model)

Why each type is a class, a record, or an enum, and why the relationships look
the way they do. See `docs/uml-class-diagram.svg` for the picture.

## 1. Classes vs records vs enums - the selection rule

I used one question to choose: **does this thing have identity plus a state
that changes over time, with behaviour that guards the changes?**

| Choice  | Rule                                        | Types |
|---------|---------------------------------------------|-------|
| class   | identity + mutable state + guarded behaviour | `Customer` (loyalty points), `Product` (stock), `Order` (lifecycle), `Payment` (payment state), `Delivery` (delivery state) |
| record  | pure immutable value / snapshot              | `Address`, `OrderItem` |
| enum    | fixed, known vocabulary of states            | `OrderStatus`, `PaymentStatus`, `DeliveryStatus` |

- **Customer is a class**, not a record: loyalty points grow when orders are
  delivered, so `earnLoyaltyPoints()` is real behaviour on mutable state.
- **Product is a class**: stock moves through `decreaseStock()/increaseStock()`,
  and `decreaseStock` refuses overselling - a record could not guard that.
- **Order is a class** and the *aggregate root*: every lifecycle move
  (`confirm/ship/deliver/cancel`) is a method that first validates the current
  state and throws `IllegalStateException` with the rule in the message.
- **Address is a record**: once written on an order it must never silently
  change; equality-by-value is exactly right. Validation still happens, in the
  compact constructor.
- **OrderItem is a record**: an immutable (product, quantity) pair; the only
  behaviour is the derived value `lineTotal()`.
- **Enums for statuses**: the states are a fixed business vocabulary. Making
  them enums makes illegal *values* unrepresentable; the owning classes then
  make illegal *transitions* impossible.

## 2. Where transitions live - deliberately NOT in the enums

I considered putting `nextStatus()` logic inside the enums. I rejected it:
whether an order may ship depends on the **payment** and the **delivery**,
which the enum cannot see. Keeping transitions inside `Order` (and `Payment`,
`Delivery` for their own small machines) keeps each rule next to the data it
needs. The enums stay dumb, printable, and testable.

## 3. Two kinds of validation, two exception types

- **Bad data** (blank name, invalid email, price <= 0, quantity out of range)
  -> `IllegalArgumentException`, thrown in constructors / record compact
  constructors. The object simply never exists in an invalid form.
- **Bad timing / state change** (ship before paid, add item after confirm,
  refund before paid) -> `IllegalStateException`, thrown by the guarded
  behaviour methods, via the single private helper `requireStatus(...)`.

That split means a caller can tell "you gave me garbage" from "you asked at
the wrong moment", and the tests assert each kind separately.

## 4. Relationships and their multiplicities

- `Customer 1 --- * Order` : a customer places many orders; an order has exactly
  one customer (constructor-enforced).
- `Order 1 ◆--- * OrderItem` : composition - items have no meaning outside
  their order; `items()` returns `List.copyOf(...)` so outsiders cannot mutate
  the aggregate.
- `OrderItem * --- 1 Product` : many lines may reference the same catalogue
  product; that is why stock lives on `Product`, not on the line.
- `Order 1 --- 0..1 Payment` and `Order 1 --- 0..1 Delivery` : attached while
  PENDING; `ship()` proves why they are optional at construction but mandatory
  at shipping time.
- `Delivery * --- 1 Address`, `Customer 1 --- 1 Address` : the address record
  is shared by value; the delivery fee comes from `Address.zone()` re-using the
  Week-1 `DeliveryZone` fee table.
- `Order` re-uses Week-1 `CustomerType` on `Customer`, and `priceBreakdown()`
  delegates to Week-1 `OrderCalculator` instead of re-implementing a single
  pricing rule - **one source of truth for money maths**.

## 5. Side effects that travel with state changes

`Order.addItem` reserves stock immediately; `Order.cancel` returns it and
refunds a paid payment; `Order.deliver` moves the delivery to DELIVERED and
awards loyalty points. Bundling these inside the transition methods makes the
rules impossible to forget - a test (`addItemReservesStockAndCancelReturnsIt`,
`deliveredOrderCannotBeCancelled`) pins each one down.

## 6. What I would add next (honest limitations)

- A money type (or BigDecimal) if this grew beyond an exercise.
- Persistence/repository interfaces so aggregates can be re-hydrated.
- An event list (`OrderShipped`, `PaymentRefunded`...) once more than one
  module reacts to transitions.
