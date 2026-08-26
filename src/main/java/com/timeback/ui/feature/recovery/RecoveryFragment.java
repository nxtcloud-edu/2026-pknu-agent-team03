package com.timeback.ui.feature.recovery;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.timeback.R;

import com.timeback.ui.domain.model.ScreenState;

import dagger.hilt.android.AndroidEntryPoint;

/**
 * UI-05 시간 되찾기 화면.
 *
 * 처리하는 ScreenState:
 * - Loading: 되찾기 상태 로딩 중
 * - Content(RecoveryViewData): 타이머 UI + 목표 선택 표시
 * - Empty: 목표 없음 → 목표 먼저 생성 안내
 * - Blocked(REPRESENTATIVE_GOAL_REQUIRED): 겹침 그룹 대표 목표 선택 필요
 * - Error: 로딩 실패
 *
 * 사용자 액션:
 * - 타이머 시작/완료
 * - 수동 시간 기록
 * - 대표 목표 선택
 */
@AndroidEntryPoint
public class RecoveryFragment extends Fragment {

    private RecoveryViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(RecoveryViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_placeholder, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        observeState();
    }

    private void observeState() {
        viewModel.state.observe(getViewLifecycleOwner(), state -> {
            if (state instanceof ScreenState.Loading) {
                // TODO: 로딩 표시
            } else if (state instanceof ScreenState.Content) {
                // TODO: RecoveryViewData로 렌더링
                //   - 선택된 목표 표시
                //   - 타이머 상태 (IDLE → 시작 버튼, RUNNING → 완료 버튼)
                //   - 수동 기록 버튼
            } else if (state instanceof ScreenState.Empty) {
                // TODO: "목표를 먼저 설정하세요" 안내
            } else if (state instanceof ScreenState.Blocked) {
                // TODO: 대표 목표 선택 다이얼로그
            } else if (state instanceof ScreenState.Error) {
                // TODO: 에러 + 재시도
            }
        });

        viewModel.actionResult.observe(getViewLifecycleOwner(), result -> {
            // TODO: 타이머/수동기록 결과 피드백
        });
    }
}
