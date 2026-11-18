"use client";

import { useState } from "react";

type ChoiceGroupProps = {
  name: string;
  legend: string;
  options: string[];
  // true면 여러 개, false면 하나만 고른다.
  multiple: boolean;
};

// 선택 칩 묶음. 고른 칩을 어둡게 보여 주려고 선택 상태를 직접 들고 있는다.
// 실제 값은 숨긴 radio/checkbox가 들고 있어서 form으로 그대로 전송된다.
export default function ChoiceGroup({
  name,
  legend,
  options,
  multiple,
}: ChoiceGroupProps) {
  const [selected, setSelected] = useState<string[]>([]);

  function toggle(option: string) {
    setSelected((current) => {
      if (!multiple) {
        return [option];
      }
      return current.includes(option)
        ? current.filter((value) => value !== option)
        : [...current, option];
    });
  }

  return (
    <fieldset style={{ border: "none" }}>
      <legend style={{ fontSize: 14, fontWeight: 700 }}>
        {legend} · {multiple ? "여러 개 선택" : "하나 선택"}
      </legend>
      <div
        style={{
          display: "flex",
          flexWrap: "wrap",
          gap: "9px 8px",
          marginTop: 10,
        }}
      >
        {options.map((option) => {
          const checked = selected.includes(option);

          return (
            <label
              key={option}
              style={{
                position: "relative",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                minWidth: 54,
                height: 32,
                padding: "0 16px",
                border: checked ? "1px solid #222" : "1px solid #ccc",
                borderRadius: 2,
                background: checked ? "#222" : "#fff",
                color: checked ? "#fff" : "#333",
                fontSize: 12,
                cursor: "pointer",
              }}
            >
              <input
                type={multiple ? "checkbox" : "radio"}
                name={name}
                value={option}
                checked={checked}
                onChange={() => toggle(option)}
                // 칩 모양으로만 보여 주고 입력 요소는 숨긴다.
                style={{
                  position: "absolute",
                  inset: 0,
                  margin: 0,
                  opacity: 0,
                  cursor: "pointer",
                }}
              />
              {option}
            </label>
          );
        })}
      </div>
    </fieldset>
  );
}
