import Pagination from "@/components/Pagination";

// 와이어프레임: 포인트 내역 (mypage/points)

const BALANCE = "1,200 P";

const CHARGE_AMOUNTS = [1000, 5000, 10000];

const PAYMENT_METHODS = [{ value: "card", label: "신용·체크카드" }];

const TRANSACTIONS = [
  { id: 1, date: "2026.11.14", description: "트렌드 정보 해금", change: "−100 P", balance: "1,200 P" },
  { id: 2, date: "2026.11.12", description: "댓글 작성 보상", change: "+50 P", balance: "1,300 P" },
  { id: 3, date: "2026.11.10", description: "콘텐츠 평가 참여", change: "+50 P", balance: "1,250 P" },
  { id: 4, date: "2026.11.05", description: "포인트 충전", change: "+1,000 P", balance: "1,200 P" },
];

// 거래 내역 전체 페이지 수 (목업)
const TOTAL_PAGES = 5;

const TRANSACTION_FILTERS = ["전체", "적립", "사용", "충전"];

// 와이어프레임에서 처음 선택된 충전 금액
const DEFAULT_AMOUNT = 5000;

export default function PointsPage() {
  return (
    <main style={{ display: "flex", flexDirection: "column", gap: 24 }}>
      <header>
        <h1 style={{ fontSize: 26, fontWeight: 700 }}>포인트 내역</h1>
        <p style={{ marginTop: 14, fontSize: 13, color: "#555" }}>
          포인트를 충전하고 적립·사용 내역을 확인하세요.
        </p>
      </header>

      <section
        aria-label="보유 포인트"
        style={{ marginTop: 12, padding: "20px 22px 30px", background: "#f2f2f2" }}
      >
        <p style={{ fontSize: 13, color: "#555" }}>현재 보유 포인트</p>
        <p style={{ marginTop: 10, fontSize: 30, fontWeight: 700 }}>{BALANCE}</p>
      </section>

      <section style={{ padding: "24px 22px 44px", border: "1px solid #999" }}>
        <h2 style={{ fontSize: 20, fontWeight: 700 }}>포인트 충전</h2>
        {/* TODO: 충전(결제) 요청 연결 */}
        <form style={{ display: "flex", flexDirection: "column", gap: 12 }}>
          <fieldset style={{ border: "none" }}>
            <legend style={{ marginTop: 14, fontSize: 13, color: "#555" }}>
              충전할 금액을 선택하세요.
            </legend>
            <div
              style={{
                display: "grid",
                gridTemplateColumns: "repeat(auto-fit, minmax(120px, 1fr))",
                gap: 8,
                marginTop: 10,
              }}
            >
              {[
                ...CHARGE_AMOUNTS.map((amount) => ({
                  value: String(amount),
                  label: `${amount.toLocaleString()} P`,
                })),
                { value: "custom", label: "직접 입력" },
              ].map((option) => {
                const selected = option.value === String(DEFAULT_AMOUNT);

                return (
                  <label
                    key={option.value}
                    style={{
                      display: "flex",
                      alignItems: "center",
                      gap: 8,
                      height: 44,
                      padding: "0 16px",
                      border: "1px solid #999",
                      background: selected ? "#f2f2f2" : "#fff",
                      fontSize: 14,
                      fontWeight: selected ? 700 : 400,
                      cursor: "pointer",
                    }}
                  >
                    <input
                      type="radio"
                      name="amount"
                      value={option.value}
                      defaultChecked={selected}
                    />
                    {option.label}
                  </label>
                );
              })}
            </div>
          </fieldset>

          <input
            type="number"
            name="customAmount"
            step={1000}
            min={1000}
            placeholder="직접 입력할 금액 (1,000 P 단위)"
            style={{
              height: 44,
              padding: "0 14px",
              border: "1px solid #999",
              fontSize: 13,
            }}
          />

          <label
            style={{
              display: "flex",
              alignItems: "center",
              height: 44,
              padding: "0 14px",
              border: "1px solid #999",
              fontSize: 13,
              fontWeight: 700,
            }}
          >
            <span style={{ width: 130 }}>결제 수단</span>
            <select
              name="paymentMethod"
              defaultValue={PAYMENT_METHODS[0].value}
              style={{
                flex: 1,
                height: "100%",
                border: "none",
                background: "none",
                color: "#555",
                fontSize: 13,
              }}
            >
              {PAYMENT_METHODS.map((method) => (
                <option key={method.value} value={method.value}>
                  {method.label}
                </option>
              ))}
            </select>
          </label>

          {/* TODO: 선택한 금액에 맞춰 갱신 */}
          <p
            style={{
              display: "flex",
              justifyContent: "space-between",
              alignItems: "center",
              height: 44,
              padding: "0 14px",
              background: "#f2f2f2",
              fontSize: 13,
            }}
          >
            <span style={{ color: "#555" }}>선택한 포인트</span>
            <span style={{ fontWeight: 700 }}>5,000 P / 5,000원</span>
          </p>

          <button
            type="submit"
            style={{
              height: 48,
              border: "none",
              background: "#1b1b1b",
              color: "#fff",
              fontSize: 14,
              fontWeight: 700,
              cursor: "pointer",
            }}
          >
            포인트 충전하기
          </button>
          <p style={{ fontSize: 12, color: "#555" }}>
            결제 완료 후 포인트가 잔액에 반영됩니다.
          </p>
        </form>
      </section>

      <section style={{ padding: "24px 22px 56px", border: "1px solid #999" }}>
        <h2 style={{ fontSize: 20, fontWeight: 700 }}>거래 내역</h2>
        {/* TODO: 필터 연결 */}
        <div style={{ display: "flex", gap: 8, marginTop: 14 }}>
          {TRANSACTION_FILTERS.map((filter, index) => (
            <button
              key={filter}
              type="button"
              aria-pressed={index === 0}
              style={{
                width: 82,
                height: 36,
                padding: "0 6px",
                border: "1px solid #999",
                background: index === 0 ? "#f2f2f2" : "#fff",
                fontSize: 13,
                fontWeight: index === 0 ? 700 : 400,
                textAlign: "left",
                cursor: "pointer",
              }}
            >
              {filter}
            </button>
          ))}
        </div>

        <div style={{ marginTop: 12, overflowX: "auto" }}>
          <table
            style={{
              width: "100%",
              minWidth: 520,
              borderCollapse: "separate",
              borderSpacing: "0 12px",
              fontSize: 13,
              textAlign: "left",
            }}
          >
            <thead>
              <tr style={{ background: "#f2f2f2" }}>
                {["날짜", "내용", "변동", "잔액"].map((heading) => (
                  <th
                    key={heading}
                    style={{ height: 36, padding: "0 12px", fontWeight: 400, color: "#555" }}
                  >
                    {heading}
                  </th>
                ))}
              </tr>
            </thead>
            <tbody>
              {TRANSACTIONS.map((transaction) => (
                <tr key={transaction.id}>
                  <td
                    style={{
                      height: 62,
                      padding: "0 12px",
                      borderTop: "1px solid #999",
                      borderBottom: "1px solid #999",
                      borderLeft: "1px solid #999",
                      fontSize: 12,
                    }}
                  >
                    {transaction.date}
                  </td>
                  <td
                    style={{
                      padding: "0 12px",
                      borderTop: "1px solid #999",
                      borderBottom: "1px solid #999",
                    }}
                  >
                    {transaction.description}
                  </td>
                  <td
                    style={{
                      padding: "0 12px",
                      borderTop: "1px solid #999",
                      borderBottom: "1px solid #999",
                      fontWeight: 700,
                    }}
                  >
                    {transaction.change}
                  </td>
                  <td
                    style={{
                      padding: "0 12px",
                      borderTop: "1px solid #999",
                      borderBottom: "1px solid #999",
                      borderRight: "1px solid #999",
                      fontSize: 12,
                      color: "#777",
                    }}
                  >
                    {transaction.balance}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {/* TODO: 실제 현재 페이지와 전체 페이지 수 연결 */}
        <Pagination currentPage={1} totalPages={TOTAL_PAGES} />
      </section>
    </main>
  );
}
