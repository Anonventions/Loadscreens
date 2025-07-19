"""
Animation Manager - Core animation system for handling playback and preview
"""

import threading
import time
from typing import List, Callable, Optional

class AnimationManager:
    """Manages animation playback, timing, and preview functionality."""
    
    def __init__(self):
        self.is_playing = False
        self.current_frame_index = 0
        self.frame_interval = 100  # milliseconds
        self.loop_animation = True
        self.playback_thread = None
        self.frame_changed_callbacks = []
        self.animation_stopped_callbacks = []
        
    def add_frame_changed_callback(self, callback: Callable[[int], None]):
        """Add callback for when frame changes during playback."""
        self.frame_changed_callbacks.append(callback)
        
    def add_animation_stopped_callback(self, callback: Callable[[], None]):
        """Add callback for when animation stops."""
        self.animation_stopped_callbacks.append(callback)
        
    def set_frame_interval(self, interval_ms: int):
        """Set the interval between frames in milliseconds."""
        self.frame_interval = max(16, interval_ms)  # Minimum 16ms (60fps)
        
    def set_loop_animation(self, loop: bool):
        """Set whether animation should loop."""
        self.loop_animation = loop
        
    def play(self, total_frames: int, start_frame: int = 0):
        """Start animation playback."""
        if self.is_playing:
            self.stop()
            
        if total_frames <= 0:
            return
            
        self.is_playing = True
        self.current_frame_index = start_frame
        
        # Start playback thread
        self.playback_thread = threading.Thread(
            target=self._playback_loop,
            args=(total_frames,),
            daemon=True
        )
        self.playback_thread.start()
        
    def stop(self):
        """Stop animation playback."""
        self.is_playing = False
        if self.playback_thread and self.playback_thread.is_alive():
            self.playback_thread.join(timeout=0.5)
            
        # Notify callbacks
        for callback in self.animation_stopped_callbacks:
            try:
                callback()
            except Exception as e:
                print(f"Error in animation stopped callback: {e}")
                
    def pause(self):
        """Pause animation playback."""
        self.is_playing = False
        
    def resume(self, total_frames: int):
        """Resume animation playback from current frame."""
        if not self.is_playing:
            self.play(total_frames, self.current_frame_index)
            
    def goto_frame(self, frame_index: int, total_frames: int):
        """Go to specific frame."""
        if 0 <= frame_index < total_frames:
            self.current_frame_index = frame_index
            self._notify_frame_changed()
            
    def next_frame(self, total_frames: int):
        """Go to next frame."""
        if total_frames > 0:
            self.current_frame_index = (self.current_frame_index + 1) % total_frames
            self._notify_frame_changed()
            
    def previous_frame(self, total_frames: int):
        """Go to previous frame."""
        if total_frames > 0:
            self.current_frame_index = (self.current_frame_index - 1) % total_frames
            self._notify_frame_changed()
            
    def _playback_loop(self, total_frames: int):
        """Main playback loop running in separate thread."""
        while self.is_playing and total_frames > 0:
            # Notify frame change
            self._notify_frame_changed()
            
            # Wait for frame interval
            time.sleep(self.frame_interval / 1000.0)
            
            # Advance to next frame
            self.current_frame_index += 1
            
            # Handle looping
            if self.current_frame_index >= total_frames:
                if self.loop_animation:
                    self.current_frame_index = 0
                else:
                    self.is_playing = False
                    break
                    
    def _notify_frame_changed(self):
        """Notify all callbacks of frame change."""
        for callback in self.frame_changed_callbacks:
            try:
                callback(self.current_frame_index)
            except Exception as e:
                print(f"Error in frame changed callback: {e}")
                
    def get_current_frame(self) -> int:
        """Get current frame index."""
        return self.current_frame_index
        
    def is_animation_playing(self) -> bool:
        """Check if animation is currently playing."""
        return self.is_playing