import pygame
import math
import random

# Initialize Pygame
pygame.init()

# Screen dimensions
WIDTH, HEIGHT = 1000, 700
screen = pygame.display.set_mode((WIDTH, HEIGHT))
pygame.display.set_caption("جنگنده سه‌بعدی فوق مدرن")

# Colors
BLACK = (0, 0, 0)
DARK_BLUE = (10, 20, 50)
BLUE = (30, 144, 255)
RED = (255, 50, 50)
WHITE = (255, 255, 255)
YELLOW = (255, 255, 0)
GRAY = (100, 100, 100)
GREEN = (0, 255, 0)  # اضافه شده

# Star field for background
stars = []
for _ in range(200):
    x = random.randint(0, WIDTH)
    y = random.randint(0, HEIGHT)
    speed = random.uniform(0.5, 3)
    size = random.randint(1, 3)
    stars.append([x, y, speed, size])

# Fighter class
class Fighter:
    def __init__(self):
        self.x = WIDTH // 2
        self.y = HEIGHT // 2
        self.z = 0  # For 3D effect
        self.speed = 5
        self.angle = 0
        self.roll = 0
        self.size = 50
        self.engine_power = 0
        self.max_engine_power = 100
        self.engine_glow = 0
        self.engine_glow_dir = 1
        
    def update(self):
        # Update engine glow effect
        self.engine_glow += 0.1 * self.engine_glow_dir
        if self.engine_glow > 1 or self.engine_glow < 0:
            self.engine_glow_dir *= -1
            
        # Move stars to simulate motion
        for star in stars:
            star[0] -= star[2] * math.cos(math.radians(self.angle))
            star[1] -= star[2] * math.sin(math.radians(self.angle))
            
            # Reset stars that go off screen
            if star[0] < 0:
                star[0] = WIDTH
                star[1] = random.randint(0, HEIGHT)
            if star[0] > WIDTH:
                star[0] = 0
                star[1] = random.randint(0, HEIGHT)
            if star[1] < 0:
                star[1] = HEIGHT
                star[0] = random.randint(0, WIDTH)
            if star[1] > HEIGHT:
                star[1] = 0
                star[0] = random.randint(0, WIDTH)
    
    def draw(self, surface):
        # Draw the fighter as a 3D shape
        center_x, center_y = self.x, self.y
        
        # Calculate points for the fighter
        nose_x = center_x + self.size * math.cos(math.radians(self.angle))
        nose_y = center_y + self.size * math.sin(math.radians(self.angle))
        
        left_wing_x = center_x + (self.size/2) * math.cos(math.radians(self.angle - 150))
        left_wing_y = center_y + (self.size/2) * math.sin(math.radians(self.angle - 150))
        
        right_wing_x = center_x + (self.size/2) * math.cos(math.radians(self.angle + 150))
        right_wing_y = center_y + (self.size/2) * math.sin(math.radians(self.angle + 150))
        
        tail_x = center_x - self.size * math.cos(math.radians(self.angle))
        tail_y = center_y - self.size * math.sin(math.radians(self.angle))
        
        # Draw the main body
        pygame.draw.polygon(surface, GRAY, [
            (nose_x, nose_y),
            (left_wing_x, left_wing_y),
            (tail_x, tail_y),
            (right_wing_x, right_wing_y)
        ])
        
        # Draw cockpit
        cockpit_x = center_x + (self.size/3) * math.cos(math.radians(self.angle))
        cockpit_y = center_y + (self.size/3) * math.sin(math.radians(self.angle))
        pygame.draw.circle(surface, BLUE, (int(cockpit_x), int(cockpit_y)), self.size//6)
        
        # Draw engine glow
        glow_size = self.size/2 * (0.7 + 0.3 * self.engine_glow)
        glow_x = tail_x - (self.size/3) * math.cos(math.radians(self.angle))
        glow_y = tail_y - (self.size/3) * math.sin(math.radians(self.angle))
        
        # Create engine glow effect
        for i in range(3):
            glow_radius = int(glow_size * (1 - i * 0.3))
            glow_color = (
                min(255, YELLOW[0] + i * 50),
                max(0, YELLOW[1] - i * 30),
                max(0, YELLOW[2] - i * 80)
            )
            pygame.draw.circle(surface, glow_color, (int(glow_x), int(glow_y)), glow_radius)
        
        # Draw wings details
        pygame.draw.line(surface, RED, (left_wing_x, left_wing_y), 
                         (left_wing_x + (self.size/4) * math.cos(math.radians(self.angle - 90)), 
                          left_wing_y + (self.size/4) * math.sin(math.radians(self.angle - 90))), 3)
        
        pygame.draw.line(surface, RED, (right_wing_x, right_wing_y), 
                         (right_wing_x + (self.size/4) * math.cos(math.radians(self.angle + 90)), 
                          right_wing_y + (self.size/4) * math.sin(math.radians(self.angle + 90))), 3)

# Create fighter
fighter = Fighter()

# Main game loop
running = True
clock = pygame.time.Clock()

while running:
    for event in pygame.event.get():
        if event.type == pygame.QUIT:
            running = False
    
    # Get keyboard input
    keys = pygame.key.get_pressed()
    
    # Rotate fighter
    if keys[pygame.K_LEFT]:
        fighter.angle -= 3
    if keys[pygame.K_RIGHT]:
        fighter.angle += 3
    
    # Move fighter forward/backward
    if keys[pygame.K_UP]:
        fighter.x += fighter.speed * math.cos(math.radians(fighter.angle))
        fighter.y += fighter.speed * math.sin(math.radians(fighter.angle))
    if keys[pygame.K_DOWN]:
        fighter.x -= fighter.speed * math.cos(math.radians(fighter.angle))
        fighter.y -= fighter.speed * math.sin(math.radians(fighter.angle))
    
    # Update fighter
    fighter.update()
    
    # Draw everything
    screen.fill(DARK_BLUE)
    
    # Draw stars
    for star in stars:
        pygame.draw.circle(screen, WHITE, (int(star[0]), int(star[1])), star[3])
    
    # Draw fighter
    fighter.draw(screen)
    
    # Draw HUD
    pygame.draw.rect(screen, (0, 100, 0, 150), (10, 10, 200, 100), 2)
    font = pygame.font.SysFont('tahoma', 16)
    speed_text = font.render(f"سرعت: {fighter.speed * 10} کیلومتر/ساعت", True, GREEN)
    altitude_text = font.render(f"ارتفاع: {1000 + fighter.y} متر", True, GREEN)
    heading_text = font.render(f"جهت: {fighter.angle % 360} درجه", True, GREEN)
    
    screen.blit(speed_text, (20, 20))
    screen.blit(altitude_text, (20, 50))
    screen.blit(heading_text, (20, 80))
    
    # Update display
    pygame.display.flip()
    clock.tick(60)

pygame.quit()
