import type { ReactNode } from "react";

// 관리자 화면에서 반복되는 작은 부품들.
// 현황 숫자 상자, 필터 버튼 줄, 검색창, 목록 표, 선택한 항목 상자.

export function AdminStats({
  stats,
}: {
  stats: { label: string; value: string }[];
}) {
  return (
    <dl
      style={{
        display: "grid",
        gridTemplateColumns: `repeat(${stats.length}, minmax(0, 1fr))`,
        gap: 12,
      }}
    >
      {stats.map((stat) => (
        <div
          key={stat.label}
          style={{
            display: "flex",
            flexDirection: "column",
            gap: 8,
            minHeight: 80,
            padding: "10px 14px",
            border: "1px solid #999",
            background: "#f4f4f4",
          }}
        >
          <dt style={{ fontSize: 12, color: "#555" }}>{stat.label}</dt>
          <dd style={{ fontSize: 24, fontWeight: 700 }}>{stat.value}</dd>
        </div>
      ))}
    </dl>
  );
}

export function AdminFilters({
  label,
  filters,
  maxWidth,
}: {
  label: string;
  filters: string[];
  // 없으면 내용 상자 폭을 꽉 채운다.
  maxWidth?: number;
}) {
  return (
    // TODO: 필터 연결
    <div
      role="group"
      aria-label={label}
      style={{
        display: "grid",
        gridTemplateColumns: `repeat(${filters.length}, minmax(0, 1fr))`,
        gap: 10,
        maxWidth,
      }}
    >
      {filters.map((filter, index) => (
        <button
          key={filter}
          type="button"
          aria-pressed={index === 0}
          style={{
            height: 34,
            padding: "0 10px",
            border: "1px solid #999",
            background: index === 0 ? "#f4f4f4" : "#fff",
            fontSize: 12,
            fontWeight: index === 0 ? 700 : 400,
            textAlign: "left",
            cursor: "pointer",
          }}
        >
          {filter}
        </button>
      ))}
    </div>
  );
}

export function AdminSearch({
  placeholder,
  button = "검색",
  name = "query",
  type = "search",
}: {
  placeholder: string;
  button?: string;
  name?: string;
  // 검색이 아닌 입력(예: 영상 URL 등록)에도 같은 모양을 쓴다.
  type?: "search" | "url";
}) {
  return (
    // TODO: 검색(또는 등록) 요청 연결
    <form
      role={type === "search" ? "search" : undefined}
      style={{ display: "flex", gap: 12 }}
    >
      <input
        type={type}
        name={name}
        aria-label={placeholder}
        placeholder={placeholder}
        style={{
          flex: 1,
          minWidth: 0,
          height: 38,
          padding: "0 12px",
          border: "1px solid #999",
          fontSize: 12,
        }}
      />
      <button
        type="submit"
        style={{
          width: 164,
          height: 38,
          padding: "0 12px",
          border: "1px solid #999",
          background: "#fff",
          fontSize: 12,
          fontWeight: 700,
          textAlign: "left",
          cursor: "pointer",
        }}
      >
        {button}
      </button>
    </form>
  );
}

export function AdminTable({
  columns,
  rows,
}: {
  // width는 표 너비에 대한 비율(%)
  columns: { label: string; width: number }[];
  rows: { id: number; cells: ReactNode[] }[];
}) {
  return (
    <div style={{ overflowX: "auto", border: "1px solid #999" }}>
      <table
        style={{
          width: "100%",
          minWidth: 640,
          borderCollapse: "collapse",
          tableLayout: "fixed",
          fontSize: 12,
          textAlign: "left",
        }}
      >
        <colgroup>
          {columns.map((column) => (
            <col key={column.label} style={{ width: `${column.width}%` }} />
          ))}
          <col style={{ width: 88 }} />
        </colgroup>
        <thead>
          <tr style={{ background: "#f4f4f4" }}>
            {[...columns.map((column) => column.label), "관리"].map((label) => (
              <th
                key={label}
                style={{
                  height: 40,
                  padding: "0 14px",
                  borderBottom: "1px solid #999",
                  fontWeight: 700,
                }}
              >
                {label}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {rows.map((row, index) => (
            <tr key={row.id}>
              {row.cells.map((cell, cellIndex) => (
                <td
                  key={cellIndex}
                  style={{
                    height: 48,
                    padding: "0 14px",
                    borderTop: index === 0 ? "none" : "1px solid #999",
                    overflow: "hidden",
                    textOverflow: "ellipsis",
                    whiteSpace: "nowrap",
                  }}
                >
                  {cell}
                </td>
              ))}
              <td
                style={{
                  padding: "0 14px",
                  borderTop: index === 0 ? "none" : "1px solid #999",
                }}
              >
                {/* TODO: 누르면 아래 '선택한 항목' 상자에 보여 주기 */}
                <button
                  type="button"
                  style={{
                    border: "none",
                    background: "none",
                    fontSize: 12,
                    cursor: "pointer",
                  }}
                >
                  보기 →
                </button>
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}

export function AdminDetail({
  title,
  rows,
  actions,
  children,
}: {
  title: string;
  rows: { label?: string; value: ReactNode }[];
  actions: string[];
  // rows 아래, 버튼 위에 넣을 내용 (예: 상태 선택)
  children?: ReactNode;
}) {
  return (
    <section style={{ padding: "14px 16px 18px", border: "1px solid #999" }}>
      <h3 style={{ fontSize: 15, fontWeight: 700 }}>{title}</h3>
      <dl
        style={{
          display: "flex",
          flexDirection: "column",
          gap: 12,
          marginTop: 14,
          fontSize: 12,
        }}
      >
        {rows.map((row, index) => (
          <div key={index} style={{ display: "flex", gap: 8 }}>
            {row.label && <dt>{row.label}</dt>}
            <dd>{row.value}</dd>
          </div>
        ))}
      </dl>
      {children}
      {/* TODO: 각 동작 연결 */}
      <div
        style={{
          display: "flex",
          justifyContent: "flex-end",
          gap: 12,
          marginTop: 20,
          paddingRight: 14,
        }}
      >
        {actions.map((action) => (
          <button
            key={action}
            type="button"
            style={{
              width: 106,
              height: 36,
              padding: "0 12px",
              border: "1px solid #999",
              background: "#fff",
              fontSize: 12,
              fontWeight: 700,
              textAlign: "left",
              cursor: "pointer",
            }}
          >
            {action}
          </button>
        ))}
      </div>
    </section>
  );
}

export function AdminNote({ children }: { children: ReactNode }) {
  return (
    <p style={{ marginTop: 16, fontSize: 11, color: "#777" }}>{children}</p>
  );
}

export function AdminHeading({
  children,
  action,
}: {
  children: ReactNode;
  // 제목 오른쪽 버튼 (예: 콘텐츠 등록)
  action?: ReactNode;
}) {
  return (
    <div
      style={{
        display: "flex",
        justifyContent: "space-between",
        alignItems: "flex-start",
        marginBottom: 16,
      }}
    >
      <h2 style={{ fontSize: 19, fontWeight: 700 }}>{children}</h2>
      {action}
    </div>
  );
}
